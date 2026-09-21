#!/usr/bin/env python3
"""Select a successfully published commit for the development GitOps overlay."""
import argparse
from pathlib import Path
import re

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("commit", help="Full 40-character Git SHA from a successful Publish images run")
args = parser.parse_args()
if not re.fullmatch(r"[0-9a-f]{40}", args.commit):
    parser.error("commit must be a full lowercase 40-character Git SHA")
path = Path(__file__).resolve().parents[1] / "deploy/gitops/environments/dev/kustomization.yaml"
original = path.read_text()
updated, count = re.subn(r"(?m)^(\s*newTag:) .+$", rf"\1 sha-{args.commit}", original)
if count != 12:
    raise SystemExit(f"Expected 12 images; found {count}. Overlay was not changed.")
path.write_text(updated)
print(f"Selected sha-{args.commit} for all {count} images. Review and commit {path}.")
