class Jdkcerts < Formula
  desc "Tool to manage JDK certificates"
  homepage "https://github.com/ADarko22/JDKCertsTool"
  url "https://github.com/ADarko22/JDKCertsTool/releases/download/v1.0.0/jdkcertstool.jar"
  version "1.0.0"
  sha256 "0019dfc4b32d63c1392aa264aed2253c1e0c2fb09216f8e2cc269bbfb8bb49b5"
  license "Apache-2.0"

  depends_on "openjdk"

  def install
    libexec.install "jdkcertstool.jar"

    (bin/"jdkcerts").write <<~EOS
      #!/bin/bash
      exec "\#{Formula["openjdk"].opt_bin}/java" -jar "\#{libexec}/jdkcertstool.jar" "$@"
    EOS
  end

  test do
    system "\#{bin}/jdkcerts", "--help"
  end
end
