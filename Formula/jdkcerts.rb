class Jdkcerts < Formula
  desc "Tool to manage JDK certificates"
  homepage "https://github.com/ADarko22/JDKCertsTool"
  url "https://github.com/ADarko22/JDKCertsTool/releases/download/v1.0.0/jdkcertstool.jar" # The primary URL for the archive/main file
  version "1.0.0"
  sha256 "21e56ec60bb8e3ee7dae22bf341ed3c3a90b08e91170ed7fc477bf9a3b55f338" # SHA for the main file
  license "Apache-2.0"

  # Define a resource for the script
  resource "jdkcerts-script" do
    url "https://github.com/ADarko22/JDKCertsTool/releases/download/v1.0.0/jdkcerts"
    sha256 "8ad9f3d4d2437417d4600d312f47665d6c6f11e3895cfdc04c722b3f496d456a"
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
