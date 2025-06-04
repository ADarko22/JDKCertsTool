#!/bin/bash

set -euo pipefail

TAG=$1
REPO="ADarko22/JDKCertsTool"
JAR_NAME="jdkcertstool.jar"
URL="https://github.com/$REPO/releases/download/$TAG/$JAR_NAME"

# Download the jar fresh (overwrite if exists)
echo "Downloading $JAR_NAME from $URL"
curl -L -o "$JAR_NAME" "$URL"

# Compute SHA256 of the downloaded jar
SHA=$(shasum -a 256 "$JAR_NAME" | awk '{print $1}')

# Ensure Formula directory exists relative to this script
FORMULA_DIR="$(dirname "$0")/../Formula"
mkdir -p "$FORMULA_DIR"

cat > "$FORMULA_DIR/jdkcerts.rb" <<EOF
class Jdkcerts < Formula
  desc "Tool to manage JDK certificates"
  homepage "https://github.com/$REPO"
  url "$URL"
  version "${TAG#v}"
  sha256 "$SHA"
  license "Apache-2.0"

  depends_on "openjdk"

  def install
    libexec.install "$JAR_NAME"

    (bin/"jdkcerts").write <<~EOS
      #!/bin/bash
      exec "\#{Formula["openjdk"].opt_bin}/java" -jar "\#{libexec}/$JAR_NAME" "\$@"
    EOS
  end

  test do
    system "\#{bin}/jdkcerts", "--help"
  end
end
EOF

echo "Formula generated at $FORMULA_DIR/jdkcerts.rb"
