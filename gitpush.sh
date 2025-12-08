#!/bin/bash

# ---------------------------------------------
# Script Name: Git Pusher Pro
# Platform: Termux / Linux
# Version: 5.0 (UI)
# Developer: MD Sirajul Islam
# ---------------------------------------------

# --- COLOR DEFINITIONS ---
RED='\033[1;31m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
CYAN='\033[1;36m'
WHITE='\033[1;37m'
MAGENTA='\033[1;35m'
NC='\033[0m' # No Color

# --- SYSTEM VARIABLES ---
CURRENT_PATH=$(pwd)
FOLDER_NAME=$(basename "$PWD")
MAX_WAIT_SECONDS=30

# --- DETECT OS ---
if [[ "$OSTYPE" == "linux-android" ]] || [ -d "/data/data/com.termux" ]; then
    DEVICE_OS="Android (Termux)"
    IS_TERMUX=true
else
    DEVICE_OS="Linux / Distro  "
    IS_TERMUX=false
fi

# --- HELPER FUNCTIONS ---

msg_info() { echo -e "${BLUE}[INFO] ${WHITE}$1...${NC}"; }
msg_error() { echo -e "${RED}[ERROR] $1${NC}"; }

clear_screen() { clear; }

# Check Internet
check_connection() {
    ping -q -c 1 -W 1 google.com >/dev/null 2>&1
}

# Wait for Internet (Loop)
ensure_online() {
    if check_connection; then return 0; fi

    echo ""
    echo -e "${YELLOW}[WARNING] Device is currently OFFLINE!${NC}"
    echo -e "${YELLOW}Waiting for connection (Max ${MAX_WAIT_SECONDS}s)...${NC}"
    
    local count=0
    while [ $count -lt $MAX_WAIT_SECONDS ]; do
        sleep 3
        count=$((count + 3))
        if check_connection; then
            echo -e "${GREEN}[SUCCESS] Connection Restored! Resuming...${NC}\n"
            return 0
        fi
        echo -e "${CYAN} -> Checking... (${count}s / ${MAX_WAIT_SECONDS}s)${NC}"
    done

    msg_error "Connection timed out. Tool is exiting."
    exit 1
}

# Display Main Banner
show_banner() {
    clear_screen
    local net_status="${RED}Offline${CYAN}"
    if check_connection; then net_status="${GREEN}Online ${CYAN}"; fi

    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════════════╗"
    echo "║                                                  ║"
    echo -e "║               ${YELLOW} GIT PUSH PRO V5.0${CYAN}                 ║"
    echo "║                                                  ║"
    echo "╠══════════════════════════════════════════════════╣"
    echo -e "║ ${WHITE}Developer :${GREEN} MD Sirajul Islam                     ${CYAN}║"
    echo -e "║ ${WHITE}Github    :${GREEN} @MSI-Sirajul                         ${CYAN}║"
    echo -e "║ ${WHITE}Device    :${YELLOW} $DEVICE_OS                     ${CYAN}║"
    echo -e "║ ${WHITE}Status    : ${net_status}                              ${CYAN}║"
    echo "╚══════════════════════════════════════════════════╝"
    echo -e "${WHITE}Current Directory: ${BLUE}$CURRENT_PATH${NC}"
    echo ""
}

# Dependency Check
check_dependencies() {
    # Simple silent check
    if [ -x "$(command -v pkg)" ]; then PM="pkg"
    elif [ -x "$(command -v apt)" ]; then PM="sudo apt"
    else PM="yum"; fi

    if ! command -v git &> /dev/null; then
        echo -e "${YELLOW}Installing Git...${NC}"
        $PM install git -y &> /dev/null
    fi
    if ! command -v gh &> /dev/null; then
        echo -e "${YELLOW}Installing GitHub CLI...${NC}"
        $PM install gh -y &> /dev/null
    fi
}

# Count Stats
get_project_stats() {
    local dir="$1"
    local f_count=0
    local d_count=0
    if [ -d "$dir" ]; then
        d_count=$(find "$dir" -type d -not -path "*/\.git/*" -not -name ".git" | wc -l)
        d_count=$((d_count - 1))
        f_count=$(find "$dir" -type f -not -path "*/\.git/*" | wc -l)
    fi
    echo "$d_count $f_count"
}

# UI: Step 1 - Analysis
ui_step_analysis() {
    clear_screen
    echo -e "${CYAN}╔══════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║              ${YELLOW}        ANALYSIS${CYAN}                    ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════╝${NC}"
    echo -e "${CYAN}≫  ${WHITE}Target Name  : ${GREEN}$3${NC}"
    echo -e "${CYAN}≫  ${WHITE}Total Folders: ${MAGENTA}$1${NC}"
    echo -e "${CYAN}≫  ${WHITE}Total Files  : ${BLUE}$2${NC}"
    
    sleep 2 # Hold screen for user to see
}

# UI: Step 2 - Scanning
ui_step_scanning() {
    local count=$1
    local type=$2
    
    # We don't clear screen here inside the loop to allow both bars to show
    # But we clear before calling this function in process_push
    local sleep_time=0.02
    if [ "$count" -gt 100 ]; then sleep_time=0.005; fi

    for ((i=0; i<=count; i++)); do
        local pct=$((i * 100 / (count > 0 ? count : 1)))
        printf "\r${CYAN}├─ ${WHITE}Scanning ${type}: ${GREEN}%3d%%${NC}" $pct
        sleep $sleep_time
    done
    echo ""
}

# UI: Step 3 - GitHub Processing Header
ui_step_github_header() {
    clear_screen
    echo -e "${CYAN}╔══════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║              ${YELLOW}        GITHUB PROCESS${CYAN}              ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════╝${NC}"
}

update_gh_step() {
    local msg=$1
    local status=$2
    if [ "$status" == "done" ]; then
        echo -e "${CYAN}  ${WHITE}✓ $msg${NC}"
    else
        echo -e "${CYAN}  ${WHITE}⏳ $msg${NC}"
    fi
    sleep 0.5 # Small delay for visual effect
}

# UI: Step 4 - Summary
ui_step_summary() {
    echo -e "${GREEN}╔══════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║              PROJECT UPLOAD SUCCESSFUL!          ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════╝${NC}"
    echo -e "${GREEN}≫  ${WHITE}Project       : ${CYAN}$1${NC}                            "
    echo -e "${GREEN}≫  ${WHITE}Stats         : ${MAGENTA}$2 Folders / ${BLUE}$3 Files${NC} "
    echo -e "${GREEN}≫  ${WHITE}GitHub User   : ${YELLOW}$4${NC}                           "
    echo -e "${GREEN}≫  ${WHITE}URL           : ${BLUE}https://github.com/$4/$1${NC}"
    echo ""
}

# --- CORE LOGIC ---

process_push() {
    local TARGET_DIR="$1"
    
    if [ ! -d "$TARGET_DIR" ]; then
        msg_error "Directory not found!"
        read -p "Press Enter..."
        return
    fi

    cd "$TARGET_DIR" || return
    local DIR_NAME=$(basename "$PWD")
    
    # --- PHASE 1: ANALYSIS ---
    read -r F_COUNT FL_COUNT <<< "$(get_project_stats "$TARGET_DIR")"
    ui_step_analysis "$F_COUNT" "$FL_COUNT" "$DIR_NAME"

    # --- PHASE 2: SCANNING UI ---
    echo -e "${CYAN}╔══════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║               ${YELLOW}        SCANNING${CYAN}                   ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════╝${NC}"
    ui_step_scanning "$F_COUNT" "Folders"
    ui_step_scanning "$FL_COUNT" "Files  "
    sleep 1

    # --- PHASE 3: GITHUB OPERATIONS ---
    ui_step_github_header

    # Git Init
    if [ ! -d ".git" ]; then git init &> /dev/null; fi
    git branch -M main &> /dev/null
    update_gh_step "Git Environment Initialized" "done"

    # Auth
    if ! gh auth status &> /dev/null; then
        echo -e "${CYAN}  ${YELLOW}⚠ Login Required...${NC}"
        gh auth login
        ui_step_github_header # Redraw header after login clears screen
    fi
    local GH_USER=$(gh api user --jq .login 2>/dev/null)
    update_gh_step "Authenticated: $GH_USER" "done"

    # Commit
    git add . &> /dev/null
    git commit -m "Auto Push V5: $(date '+%Y-%m-%d %H:%M:%S')" &> /dev/null
    update_gh_step "Files Committed Locally" "done"

    # Repo & Push
    if gh repo view "$GH_USER/$DIR_NAME" &> /dev/null; then
        if ! git remote | grep -q "origin"; then
            git remote add origin "https://github.com/$GH_USER/$DIR_NAME.git" &> /dev/null
        else
            git remote set-url origin "https://github.com/$GH_USER/$DIR_NAME.git" &> /dev/null
        fi
        
        if git push -u origin main &> /dev/null; then
             update_gh_step "Remote Repository Updated" "done"
        else
             update_gh_step "Syncing Conflicts (Force)" "done"
             git push -u origin main --force &> /dev/null
        fi
    else
        gh repo create "$DIR_NAME" --public --source=. --remote=origin --push &> /dev/null
        update_gh_step "New Repository Created" "done"
    fi

    sleep 1.5

    # --- PHASE 4: FINAL SUMMARY ---
    ui_step_summary "$DIR_NAME" "$F_COUNT" "$FL_COUNT" "$GH_USER"
    
    read -p "Press Enter to return to menu..."
}

# --- MAIN LOOP ---
check_dependencies

while true; do
    show_banner
    
    echo -e "${YELLOW}╔════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║      Select Action to Push         ║${NC}"
    echo -e "${YELLOW}╚════════════════════════════════════╝${NC}"
    echo -e "  ${CYAN}⟨${WHITE}01${CYAN}⟩ ${GREEN}This Project${WHITE} ⟩ ${BLUE}$FOLDER_NAME${WHITE}"
    echo -e "  ${CYAN}⟨${WHITE}02${CYAN}⟩ ${GREEN}Other Project"
    echo -e "  ${CYAN}⟨${WHITE}03${CYAN}⟩ ${RED}Exit Tool"
    echo ""
    
    echo -ne "${MAGENTA}  ⟩➤ Select Option (${WHITE}1-3${MAGENTA}): ${NC}"
    read -r OPTION

    case $OPTION in
        1|01)
            ensure_online
            # Directly process, function will handle clearing
            process_push "$CURRENT_PATH"
            ;;
        2|02)
            echo ""
            if [ "$IS_TERMUX" = true ]; then
                echo -e "${CYAN}[TERMUX DETECTED]${NC}"
                echo -ne "${MAGENTA}  ⟩➤ Enter Project Name (in HOME): ${NC}"
                read -r PROJ_NAME
                TARGET_PATH="$HOME/$PROJ_NAME"
            else
                echo -e "${CYAN}[LINUX DETECTED]${NC}"
                echo -ne "${MAGENTA}  ⟩➤ Enter Full Project Path: ${NC}"
                read -r TARGET_PATH
            fi
            ensure_online
            # Process will start by clearing screen automatically
            process_push "$TARGET_PATH"
            ;;
        3|03)
            echo -e "\n${GREEN}  Thanks for using Auto Push Pro!${NC}\n"
            exit 0
            ;;
        *)
            echo -e "\n${RED}  [!] Invalid Input!${NC}"
            sleep 1
            ;;
    esac
done
