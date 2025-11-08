#!/bin/bash

# Git Browser Commands - Script để mở commit trên browser

# Lấy remote URL
REPO_URL=$(git config --get remote.origin.url | sed "s/\.git$//")

# Hàm mở repository trên browser
view_repo() {
    echo "Opening repository: $REPO_URL"
    open "$REPO_URL"
}

# Hàm mở commit cụ thể trên browser
view_commit() {
    if [ -z "$1" ]; then
        COMMIT=$(git rev-parse HEAD)
        echo "No commit specified, using current HEAD: $COMMIT"
    else
        COMMIT="$1"
    fi
    echo "Opening commit: $COMMIT"
    open "${REPO_URL}/commit/${COMMIT}"
}

# Hàm mở danh sách commit trên branch hiện tại
view_commits() {
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
    echo "Opening commits on branch: $BRANCH"
    open "${REPO_URL}/commits/${BRANCH}"
}

# Hàm mở file cụ thể trên browser
view_file() {
    if [ -z "$1" ]; then
        echo "Usage: view_file <file_path> [branch]"
        return 1
    fi
    FILE="$1"
    BRANCH="${2:-$(git rev-parse --abbrev-ref HEAD)}"
    echo "Opening file: $FILE on branch: $BRANCH"
    open "${REPO_URL}/blob/${BRANCH}/${FILE}"
}

# Hàm mở compare giữa 2 commit
view_compare() {
    if [ -z "$1" ] || [ -z "$2" ]; then
        echo "Usage: view_compare <commit1> <commit2>"
        return 1
    fi
    COMMIT1="$1"
    COMMIT2="$2"
    echo "Opening compare: $COMMIT1..$COMMIT2"
    open "${REPO_URL}/compare/${COMMIT1}..${COMMIT2}"
}

# Main menu
case "$1" in
    repo)
        view_repo
        ;;
    commit)
        view_commit "$2"
        ;;
    commits)
        view_commits
        ;;
    file)
        view_file "$2" "$3"
        ;;
    compare)
        view_compare "$2" "$3"
        ;;
    *)
        echo "Git Browser Commands"
        echo "Usage: $0 {repo|commit|commits|file|compare}"
        echo ""
        echo "Commands:"
        echo "  repo                    - Mở repository trên browser"
        echo "  commit [hash]           - Mở commit cụ thể (mặc định: HEAD)"
        echo "  commits                 - Mở danh sách commit trên branch hiện tại"
        echo "  file <path> [branch]    - Mở file cụ thể trên browser"
        echo "  compare <commit1> <commit2> - So sánh 2 commit"
        echo ""
        echo "Examples:"
        echo "  $0 repo"
        echo "  $0 commit bee5ce0"
        echo "  $0 commits"
        echo "  $0 file pom.xml"
        echo "  $0 compare bee5ce0 b978ff1"
        ;;
esac


