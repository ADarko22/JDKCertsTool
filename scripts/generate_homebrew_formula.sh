#!/bin/bash

set -euo pipefail

TAG=$1
REPO="ADarko22/JDKCertsTool"
JAR_NAME="jdkcertstool.jar"
SCRIPT_NAME="jdkcerts" # Name of the script asset
JAR_URL="https://github.com/$REPO/releases/download/$TAG/$JAR_NAME"
SCRIPT_URL="https://github.com/$REPO/releases/download/$TAG/$SCRIPT_NAME"

# Download the jar fresh (overwrite if exists)
echo "Downloading $JAR_NAME from $JAR_URL"
curl -L -o "$JAR_NAME" "$JAR_URL"

# Download the script fresh (overwrite if exists)
echo "Downloading $SCRIPT_NAME from $SCRIPT_URL"
curl -L -o "$SCRIPT_NAME" "$SCRIPT_URL"
chmod +x "$SCRIPT_NAME" # Ensure it's executable for local testing if needed

# Compute SHA256 of the downloaded jar
JAR_SHA=$(shasum -a 256 "$JAR_NAME" | awk '{print $1}')
SCRIPT_SHA=$(shasum -a 256 "$SCRIPT_NAME" | awk '{print $1}') # Also get SHA for the script

# Ensure Formula directory exists relative to this script
FORMULA_DIR="$(dirname "$0")/../Formula"
mkdir -p "$FORMULA_DIR"

cat > "$FORMULA_DIR/jdkcerts.rb" <<EOF
# frozen_string_literal: true

class Jdkcerts < Formula
  desc "Tool to manage JDK certificates"
  homepage "https://github.com/$REPO"
  url "$JAR_URL"
  version "${TAG#v}"
  sha256 "$JAR_SHA"
  license "Apache-2.0"

  resource "jdkcerts-script" do
    url "$SCRIPT_URL"
    sha256 "$SCRIPT_SHA"
  end

  depends_on "openjdk"

  def install
    libexec.install "$JAR_NAME"

    resource("jdkcerts-script").stage do
      bin.install "jdkcerts"
      chmod 0755, bin/"jdkcerts"
    end
  end

  test do
    assert_match "Usage", shell_output("\#{bin}/jdkcerts --help")
  end
end
EOF

echo "Formula generated at $FORMULA_DIR/jdkcerts.rb"