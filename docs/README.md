# Documentation Maintenance

This project uses [MkDocs](https://www.mkdocs.org/) to generate static documentation from Markdown files located under
the `docs/` folder.

---

## Quick Start (Local Preview)

### 1. Prerequisites

Ensure you have **Python 3.8+** installed on your system.

### 2. Setup Virtual Environment

Run these commands from the project root:

```bash
# Create the virtual environment
python3 -m venv .venv

# Activate it (Linux/macOS)
source .venv/bin/activate
```

### 3. Install Dependencies

Install the locked packages (with hash verification enabled):

```bash
pip install -r docs/requirements.txt
```

### 4. Run Live Preview Server

Start the local development server:

```bash
mkdocs serve
```

Open [http://127.0.0.1:8000](http://127.0.0.1:8000) in your browser.
Any changes made to files in `docs/` will automatically trigger a browser refresh.

---

## Managing & Updating Dependencies

We use `pip-tools` to manage dependencies securely.

* **`docs/requirements.in`**: Contains high-level package names (e.g., `mkdocs`, `mkdocs-material`).
* **`docs/requirements.txt`**: Auto-generated lockfile with exact versions and SHA-256 hashes.

### Adding a New Plugin or Package

1. Add the package name to `docs/requirements.in`.
2. Activate your virtual environment and install `pip-tools`:
   ```bash
   pip install pip-tools
   ```
3. Re-generate `requirements.txt` with updated hashes:
   ```bash
   pip-compile --generate-hashes docs/requirements.in -o docs/requirements.txt
   ```
4. Install the newly compiled requirements:
   ```bash
   pip install -r docs/requirements.txt
   ```

---

## Building Production HTML

To generate static HTML files for deployment (output goes to `site/` folder):

```bash
mkdocs build
```