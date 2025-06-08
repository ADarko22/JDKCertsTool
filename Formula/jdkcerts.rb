class Jdkcerts < Formula
  desc "Tool to manage JDK certificates"
  homepage "https://github.com/ADarko22/JDKCertsTool"
  url "https://github.com/ADarko22/JDKCertsTool/releases/download/v1.0.0/jdkcertstool.jar" # The primary URL for the archive/main file
  version "1.0.0"
  sha256 "f495941bd0a358a654f6c597bf2e9e0a597827f349b066055a7ad8f33d90e061" # SHA for the main file
  license "Apache-2.0"

  # Define a resource for the script
  resource "jdkcerts-script" do
    url "https://github.com/ADarko22/JDKCertsTool/releases/download/v1.0.0/jdkcerts"
    sha256 "d6d60fa6563903f2dfb2b35c2a7f403cd697a8a2d78b6dba87c2b78bb30c4513"
  end

  depends_on "openjdk"

  def install
    libexec.install "jdkcertstool.jar"

    # Install the resource script
    resource("jdkcerts-script").stage do
      bin.install "jdkcerts"
      chmod 0755, bin/"jdkcerts"
    end
  end

  test do
    system "\#{bin}/jdkcerts", "--help"
  end
end
