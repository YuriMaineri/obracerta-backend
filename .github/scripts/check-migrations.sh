#!/usr/bin/env bash
# Uso: check-migrations.sh [commit-base]
set -euo pipefail

DIR="src/main/resources/db/migration"
BASE="${1:-}"
failed=0

for file in "$DIR"/*; do
  name=$(basename "$file")
  if [[ ! "$name" =~ ^V[0-9]+__[A-Za-z0-9_]+\.sql$ ]]; then
    echo "::error file=$file::Nome fora do padrão V<numero>__<descricao>.sql (o Flyway ignoraria este arquivo)"
    failed=1
  fi
done

duplicates=$(ls "$DIR" | grep -oE '^V[0-9]+' | sort | uniq -d || true)
if [[ -n "$duplicates" ]]; then
  echo "::error::Versões de migration repetidas: $duplicates"
  failed=1
fi

if [[ -n "$BASE" ]] && git cat-file -e "$BASE^{commit}" 2>/dev/null; then
  changed=$(git diff --name-status --diff-filter=MDR "$BASE" HEAD -- "$DIR" || true)
  if [[ -n "$changed" ]]; then
    echo "::error::Migrations já publicadas não podem ser alteradas, renomeadas ou removidas. Crie uma nova versão (V2__...)."
    echo "$changed"
    failed=1
  fi
else
  echo "Sem commit base para comparar; checando apenas nomes e versões."
fi

if [[ $failed -eq 0 ]]; then
  echo "Migrations OK."
fi
exit $failed
