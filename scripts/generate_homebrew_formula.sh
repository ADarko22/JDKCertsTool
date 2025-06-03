#!/bin/bash

TAG=$1
REPO="adarko22/JDKCertsTool"
JAR_NAME="jdkcertstool.jar"
URL="https://github.com/$REPO/releases/download/$TAG/$JAR_NAME"

echo "Downloading $JAR_NAME from $URL"
curl -LO "$URL"

SHA=$(shasum -a 256 $JAR_NAME | awk '{print $1}')

cat > Formula/jdkcertstool.rb <<EOF
class Jdkcertstool < Formula
  desc "Tool to manage JDK certificates"
  homepage "https://github.com/$REPO"
  url "$URL"
  version "${TAG#v}"
  sha256 "$SHA"
  license "MIT"

  depends_on "openjdk"

  def install
    libexec.install "jdkcertstool.jar"

    (bin/"jdkcertstool").write <<~EOS
      #!/bin/bash
      exec "\#{Formula["openjdk"].opt_bin}/java" -jar "\#{libexec}/jdkcertstool.jar" "\$@"
    EOS
  end

  test do
    system "\#{bin}/jdkcertstool", "--help"
  end
end
EOF
