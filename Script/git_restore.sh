#!/bin/bash
echo "###############################"
echo "将当前目录下的Git项目全部恢复到默认分支"
echo "###############################"
echo "###############################"
echo "###############################"


# 获取当前目录
current_dir=$(pwd)

# 遍历当前目录的子目录
for subdir in */; do
    # 进入子目录
    cd "$current_dir/$subdir"

    # 检查是否存在 .git 目录
    if [ -d ".git" ]; then
        # 获取默认分支名
        default_branch=$(git symbolic-ref refs/remotes/origin/HEAD | sed 's@^refs/remotes/origin/@@')

        # 恢复到默认分支
        echo "恢复 $subdir 到默认分支: $default_branch"
        git reset --hard "$default_branch"
        git clean -df
        git pull origin "$default_branch"
    else
        echo "$subdir 不包含 Git 仓库，跳过..."
    fi

    # 检查是否存在 .git/index.lock 文件
    if [ -e ".git/index.lock" ]; then
        # 存在锁文件，删除它
        echo "删除锁文件: $subdir"
        rm -f .git/index.lock
    fi

    # 返回到上级目录
    cd "$current_dir"
done

echo "所有子目录的Git恢复完成！"
