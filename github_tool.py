#!/usr/bin/env python3
import os
import subprocess
from InquirerPy import inquirer
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.text import Text

console = Console()

# ---------- Utility Functions ----------

def run_command(cmd):
    try:
        result = subprocess.check_output(cmd, shell=True, stderr=subprocess.STDOUT)
        return result.decode("utf-8").strip()
    except subprocess.CalledProcessError as e:
        console.print(f"[bold red]Error:[/bold red] {e.output.decode('utf-8')}")
        return None


def check_gh():
    try:
        output = subprocess.check_output(["gh", "--version"], stderr=subprocess.STDOUT)
        if b"github" in output.lower():
            console.print("[bold green]GitHub CLI detected successfully.[/bold green]\n")
            return True
        else:
            raise Exception("gh not working")
    except Exception:
        console.print("[bold red]GitHub CLI (gh) not found or not accessible![/bold red]")
        console.print("Install it with: [green]pkg install gh -y[/green]")
        exit()


# ---------- GitHub Operations ----------

def create_repo():
    name = input("Enter repository name: ").strip()
    desc = input("Enter repository description: ").strip()
    private = inquirer.confirm(message="Make repository private?", default=False).execute()
    command = f'gh repo create "{name}" {"--private" if private else "--public"} -d "{desc}"'
    result = run_command(command)
    if result:
        console.print(f"[bold green]Repository '{name}' created successfully.[/bold green]\n")


def list_repos():
    data = run_command("gh repo list --limit 100 --json name -q '.[].name'")
    if data:
        repos = data.splitlines()
        table = Table(title="Your GitHub Repositories", show_header=True, header_style="bold cyan")
        table.add_column("No", justify="center")
        table.add_column("Repository Name", style="bold yellow")
        for i, repo in enumerate(repos, 1):
            table.add_row(str(i), repo)
        console.print(table)
    else:
        console.print("[bold red]No repositories found or failed to fetch list.[/bold red]\n")


def delete_repo():
    data = run_command("gh repo list --limit 100 --json name -q '.[].name'")
    if not data:
        console.print("[bold red]No repositories found.[/bold red]")
        return
    repos = data.splitlines()
    choice = inquirer.select(message="Select a repository to delete:", choices=repos).execute()
    confirm = inquirer.confirm(message=f"Are you sure you want to delete '{choice}'?", default=False).execute()
    if confirm:
        run_command(f'gh repo delete "{choice}" --yes')
        console.print(f"[bold red]Repository '{choice}' deleted successfully.[/bold red]\n")
    else:
        console.print("[yellow]Deletion cancelled.[/yellow]\n")


# ---------- Git Operations ----------

def git_push():
    home = os.path.expanduser("~")
    folders = [f for f in os.listdir(home) if os.path.isdir(os.path.join(home, f))]
    if not folders:
        console.print("[bold red]No folders found in home directory.[/bold red]")
        return
    folder = inquirer.select(message="Select a folder to push:", choices=folders).execute()
    path = os.path.join(home, folder)
    os.chdir(path)
    run_command("git add .")
    msg = input("Enter commit message: ").strip()
    run_command(f'git commit -m "{msg}"')
    run_command("git push")
    console.print("[bold green]Project pushed successfully.[/bold green]\n")


def git_commit():
    msg = input("Enter commit message: ").strip()
    run_command("git add .")
    run_command(f'git commit -m "{msg}"')
    console.print("[bold green]Changes committed successfully.[/bold green]\n")


def git_remote():
    url = input("Enter your GitHub repository URL: ").strip()
    run_command(f'git remote add origin "{url}"')
    console.print(f"[bold green]Remote set to {url}[/bold green]\n")


def git_branch():
    name = input("Enter new branch name: ").strip()
    run_command(f'git branch "{name}"')
    console.print(f"[bold green]Branch '{name}' created successfully.[/bold green]\n")


# ---------- Banner ----------

def show_banner():
    banner_text = Text()
    banner_text.append("=====================================\n", style="bold cyan")
    banner_text.append("         GitHub CLI Automation Tool\n", style="bold yellow")
    banner_text.append("        Author: MSI Sirajul Islam\n", style="green")
    banner_text.append("=====================================\n", style="bold cyan")
    console.print(Panel(banner_text, border_style="cyan"))


# ---------- Main Menu ----------

def main_menu():
    os.system("clear")
    show_banner()
    check_gh()

    while True:
        choice = inquirer.select(
            message="Select an action:",
            choices=[
                "1. Create Repository",
                "2. List Repositories",
                "3. Delete Repository",
                "4. Git Push",
                "5. Git Commit",
                "6. Git Remote",
                "7. Git Branch",
                "8. Exit"
            ],
            default="1. Create Repository"
        ).execute()

        if choice.startswith("1"):
            create_repo()
        elif choice.startswith("2"):
            list_repos()
        elif choice.startswith("3"):
            delete_repo()
        elif choice.startswith("4"):
            git_push()
        elif choice.startswith("5"):
            git_commit()
        elif choice.startswith("6"):
            git_remote()
        elif choice.startswith("7"):
            git_branch()
        elif choice.startswith("8"):
            console.print("[bold cyan]Exiting tool...[/bold cyan]")
            break


# ---------- Run Program ----------
if __name__ == "__main__":
    main_menu()
