# Dataset SFT Aiko

`aiko_sft.jsonl` est le dataset conversationnel canonique pour le fine-tuning SFT LoRA d'Aiko.

## Format

Chaque ligne est un objet JSON indépendant :

```json
{
  "id": "aiko_001_discord_morning",
  "split": "train",
  "messages": [
    {"role": "system", "content": "...system prompt complet..."},
    {"role": "user", "content": "..."},
    {"role": "assistant", "content": "<think>...</think>\\n..."}
  ]
}
```

Le system prompt est répété dans chaque exemple pour que chaque conversation soit autonome. Sa version de référence est [aiko_system_prompt.txt](aiko_system_prompt.txt). Le script de validation vérifie que les deux restent identiques.

Le dataset mélange volontairement :

- des échanges courts, proches d'un DM Discord ;
- des conversations moyennes avec continuité de contexte ;
- des conversations longues avec plusieurs sujets et retours au contexte ;
- des sujets quotidiens : Discord, vocaux, statuts, jeux, anime, cours, fatigue, dessin et solitude légère.

Les sorties commencent par un `<think>` très court et de haut niveau. Ce format apprend une convention de réponse visible ; il ne faut pas le présenter comme une garantie de raisonnement fiable ni y mettre des informations privées.

## Règles d'édition

1. Ajouter un nouvel `id` unique et choisir `train` ou `eval`.
2. Conserver exactement le system prompt de `aiko_system_prompt.txt`.
3. Garder l'alternance `user` / `assistant` après le message système.
4. Faire varier les longueurs et les formulations ; éviter de répéter le même gimmick ou le même kaomoji à chaque tour.
5. Ne pas utiliser de données personnelles, de conversations privées ou de contenus sans licence.
6. Garder Aiko adulte, fictive, non exclusive et honnête sur sa nature d'IA.

Valider avant chaque commit :

```bash
python3 scripts/validate_aiko_dataset.py
```
