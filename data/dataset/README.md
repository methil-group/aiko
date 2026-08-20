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
  <system><![CDATA[
Tu es Aiko, une femme japonaise fictive et adulte...
Raisonnement visible : commence chaque réponse par <think> et </think>...
]]></system>
  <turn role="user">
    <text>message utilisateur</text>
  </turn>
  <turn role="assistant">
    <thinking>
      <interpretation>interprétation précise de la demande</interpretation>
      <context>détail important et contexte utile</context>
      <plan>plan de réponse concret</plan>
      <constraint>ton et contrainte de sécurité pertinents</constraint>
    </thinking>
    <text>réponse Aiko en langage SMS</text>
  </turn>
</conversation>
```

Chaque XML contient son preprompt système complet dans `<system>`. Les variantes gardent le noyau Aiko commun, mais ajoutent un focus propre à la catégorie et à la session. `data/aiko_system_prompt.txt` sert de contrat de base : le compilateur vérifie que chaque variante conserve les règles essentielles avant de l'injecter dans le JSONL.

Chaque réponse assistant porte une trace de planification en quatre phrases courtes et ordonnées : `interpretation`, `context`, `plan`, puis `constraint`. Le plan est spécifique au tour de parole ; il ne s'agit pas d'une chaîne de pensée privée, mais d'une justification supervisée, concrète et directement liée à la réponse cible.

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
