# Aiko XML dataset

Chaque fichier XML représente une conversation autonome. Les fichiers sont classés par catégorie afin de pouvoir enrichir une compétence sans mélanger tout le corpus :

```text
data/dataset/
├── 01_discord/
├── 02_daily_life/
├── 03_interests/
├── 04_relationships/
├── 05_emotional_support/
└── 06_identity_and_limits/
```

## Schéma d'un fichier

```xml
<?xml version="1.0" encoding="UTF-8"?>
<conversation id="aiko_001_example" category="01_discord" split="train">
  <summary>Résumé lisible de l'intention de la conversation.</summary>
  <turn role="user">
    <text>message utilisateur</text>
  </turn>
  <turn role="assistant">
    <thinking>
      <intent>intention de réponse</intent>
      <context>détail important de la question reçue</context>
      <strategy>plan de réponse concret</strategy>
      <style>ton et contrainte de sécurité pertinents</style>
    </thinking>
    <text>réponse Aiko en langage SMS</text>
  </turn>
</conversation>
```

Le preprompt système n'est pas copié dans les XML : il est maintenu dans `data/aiko_system_prompt.txt`, puis injecté automatiquement dans chaque ligne JSONL par le compilateur. Cela évite les divergences entre catégories tout en gardant le JSONL autonome.

## Compiler

Depuis la racine du dépôt :

```bash
python3 scripts/compile_dataset.py
```

Vérifier sans modifier le JSONL :

```bash
python3 scripts/compile_dataset.py --check
```

Le compilateur parcourt récursivement tous les XML, les trie par chemin, vérifie l'alternance `user` / `assistant`, impose une fin assistant, transforme les sections de thinking en un bloc `<think>...</think>` labellisé et écrit `data/aiko_sft.jsonl`.
