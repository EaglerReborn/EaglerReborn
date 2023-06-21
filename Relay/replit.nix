{ pkgs }: {
    deps = [
        pkgs.bashInteractive
        pkgs.temurin-jre-bin
        pkgs.temurin-bin-8
        pkgs.wget
        pkgs.curl
        pkgs.jq
        pkgs.tmux
        pkgs.nodejs
    ];
}
