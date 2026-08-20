#!/usr/bin/env python3
"""Validate the local Aiko SFT JSONL without third-party dependencies."""

from __future__ import annotations

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PROMPT_PATH = ROOT / "data" / "aiko_system_prompt.txt"
DATASET_PATH = ROOT / "data" / "aiko_sft.jsonl"
THINK_RE = re.compile(
    r"^<think>Interprétation: (?P<interpretation>.*?) "
    r"Contexte: (?P<context>.*?) Plan: (?P<plan>.*?) "
    r"Contrainte: (?P<constraint>.*?)</think>\n.+",
    re.DOTALL,
)
THINKING_LABELS = ("Interprétation:", "Contexte:", "Plan:", "Contrainte:")
ROLES = ("user", "assistant")


def fail(message: str) -> None:
    raise SystemExit(f"ERROR: {message}")


def main() -> None:
    base_prompt = PROMPT_PATH.read_text(encoding="utf-8").strip()
    required_markers = ("Tu es Aiko", "Raisonnement visible", "Frontières", "Sécurité")
    rows = []
    seen_ids = set()

    with DATASET_PATH.open(encoding="utf-8") as stream:
        for line_number, line in enumerate(stream, start=1):
            if not line.strip():
                fail(f"blank line at {line_number}")
            try:
                row = json.loads(line)
            except json.JSONDecodeError as exc:
                fail(f"invalid JSON at line {line_number}: {exc}")

            row_id = row.get("id")
            if not isinstance(row_id, str) or not row_id:
                fail(f"missing id at line {line_number}")
            if row_id in seen_ids:
                fail(f"duplicate id: {row_id}")
            seen_ids.add(row_id)

            if row.get("split") not in {"train", "eval"}:
                fail(f"invalid split for {row_id}")
            if not isinstance(row.get("category"), str) or not row["category"]:
                fail(f"missing category for {row_id}")
            if not isinstance(row.get("source"), str) or not row["source"].endswith(".xml"):
                fail(f"missing XML source for {row_id}")

            messages = row.get("messages")
            if not isinstance(messages, list) or len(messages) < 3:
                fail(f"conversation too short: {row_id}")
            system_message = messages[0]
            if system_message.get("role") != "system" or not isinstance(system_message.get("content"), str):
                fail(f"missing system prompt: {row_id}")
            if any(marker not in base_prompt or marker not in system_message["content"] for marker in required_markers):
                fail(f"incomplete system prompt: {row_id}")

            previous_role = None
            for message in messages[1:]:
                role = message.get("role")
                content = message.get("content")
                if role not in ROLES or role == previous_role:
                    fail(f"invalid role alternation in {row_id}")
                if not isinstance(content, str) or not content.strip():
                    fail(f"empty message in {row_id}")
                thinking_match = THINK_RE.match(content) if role == "assistant" else None
                if role == "assistant" and not thinking_match:
                    fail(f"assistant message missing clean <think> format in {row_id}")
                if role == "assistant":
                    if not all(label in content for label in THINKING_LABELS):
                        fail(f"assistant thinking is too shallow in {row_id}")
                    sections = thinking_match.groupdict().values()
                    if any(not section.strip().endswith(".") for section in sections):
                        fail(f"assistant thinking must contain four complete sentences in {row_id}")
                previous_role = role

            if messages[-1]["role"] != "assistant":
                fail(f"conversation must end with assistant: {row_id}")
            rows.append(row)

    if len({row["messages"][0]["content"] for row in rows}) < 2:
        fail("system prompt variants are required")

    counts = {split: sum(row["split"] == split for row in rows) for split in ("train", "eval")}
    if not counts["train"] or not counts["eval"]:
        fail("both train and eval splits are required")

    print(f"OK: {len(rows)} conversations | train={counts['train']} | eval={counts['eval']}")
    print(f"OK: {sum(len(row['messages']) - 1 for row in rows)} user/assistant turns")


if __name__ == "__main__":
    main()
