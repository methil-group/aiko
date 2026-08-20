#!/usr/bin/env python3
"""Compile the categorized Aiko XML conversations into deterministic JSONL."""

from __future__ import annotations

import argparse
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ID_RE = re.compile(r"^[A-Za-z0-9][A-Za-z0-9_.-]*$")


def fail(path: Path, message: str) -> "NoReturn":
    raise ValueError(f"{path}: {message}")


def clean_text(node: ET.Element | None, path: Path, label: str) -> str:
    if node is None:
        fail(path, f"missing <{label}>")
    text = " ".join("".join(node.itertext()).split())
    if not text:
        fail(path, f"empty <{label}>")
    return text


def compile_xml(path: Path, dataset_dir: Path, system_prompt: str) -> dict:
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError as exc:
        fail(path, f"invalid XML: {exc}")

    if root.tag != "conversation":
        fail(path, "root element must be <conversation>")

    row_id = root.attrib.get("id", "")
    if not ID_RE.fullmatch(row_id):
        fail(path, "id must contain only letters, numbers, dots, underscores or hyphens")

    split = root.attrib.get("split", "train")
    if split not in {"train", "eval"}:
        fail(path, "split must be train or eval")

    category = root.attrib.get("category") or path.parent.name
    if not category:
        fail(path, "category is missing")

    turns = root.findall("./turn")
    if len(turns) < 1:
        fail(path, "conversation must contain at least one user/assistant pair")

    messages = [{"role": "system", "content": system_prompt}]
    previous_role = None
    for turn in turns:
        role = turn.attrib.get("role")
        if role not in {"user", "assistant"} or role == previous_role:
            fail(path, "turn roles must alternate user and assistant")

        if role == "user":
            if turn.find("./thinking") is not None:
                fail(path, "user turns cannot contain <thinking>")
            content = clean_text(turn.find("./text"), path, "text")
        else:
            thinking = clean_text(turn.find("./thinking"), path, "thinking")
            answer = clean_text(turn.find("./text"), path, "text")
            content = f"<think>{thinking}</think>\n{answer}"

        messages.append({"role": role, "content": content})
        previous_role = role

    if previous_role != "assistant":
        fail(path, "conversation must end with an assistant turn")

    source = path.relative_to(dataset_dir).as_posix()
    return {
        "id": row_id,
        "split": split,
        "category": category,
        "source": source,
        "summary": clean_text(root.find("./summary"), path, "summary"),
        "messages": messages,
    }


def compile_dataset(dataset_dir: Path, prompt_path: Path) -> list[dict]:
    if not dataset_dir.is_dir():
        raise ValueError(f"dataset directory does not exist: {dataset_dir}")
    system_prompt = prompt_path.read_text(encoding="utf-8").strip()
    if not system_prompt:
        raise ValueError(f"system prompt is empty: {prompt_path}")

    files = sorted(dataset_dir.rglob("*.xml"))
    if not files:
        raise ValueError(f"no XML files found below {dataset_dir}")

    rows = [compile_xml(path, dataset_dir, system_prompt) for path in files]
    ids = [row["id"] for row in rows]
    if len(ids) != len(set(ids)):
        duplicates = sorted({row_id for row_id in ids if ids.count(row_id) > 1})
        raise ValueError(f"duplicate conversation ids: {', '.join(duplicates)}")
    return rows


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input-dir", type=Path, default=ROOT / "data" / "dataset")
    parser.add_argument("--prompt", type=Path, default=ROOT / "data" / "aiko_system_prompt.txt")
    parser.add_argument("--output", type=Path, default=ROOT / "data" / "aiko_sft.jsonl")
    parser.add_argument("--check", action="store_true", help="validate without writing JSONL")
    args = parser.parse_args()

    try:
        rows = compile_dataset(args.input_dir.resolve(), args.prompt.resolve())
    except (OSError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1

    if not args.check:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        payload = "\n".join(json.dumps(row, ensure_ascii=False, separators=(",", ":")) for row in rows)
        args.output.write_text(payload + "\n", encoding="utf-8")

    counts = {split: sum(row["split"] == split for row in rows) for split in ("train", "eval")}
    print(f"OK: {len(rows)} XML conversations | train={counts['train']} | eval={counts['eval']}")
    if not args.check:
        print(f"Wrote: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
