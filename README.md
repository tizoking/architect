# architect
关于一些所谓架构师视频观看后的心得体会或者说总结什么的
> 这些体会将是以笔记 + 代码的形式进行记录<br />
> 整个视频过程中所有的东西都将记录到此仓库



### git的简单使用

1. `git clone url`(仓库的链接)------在你想克隆到的路径输入
2. `git add README.md` //文件添加到仓库
3. `git add .` //不但可以跟单一文件，还可以跟通配符，更可以跟目录。一个点就把当前目录下所有未追踪的文件全部add了 
4. `git commit -m "first commit"` //把文件提交到仓库
5. `git push -u origin master` //把本地库的所有内容推送到远程库上
6. `git pull`
7. `git init` //把这个目录变成Git可以管理的仓库
8. `git remote add origin url` //关联远程仓库
9. `git pull origin master`获取远程仓库master分支上的内容
10. `git branch --set-upstream-to=origin/master master`将当前分支设置为远程仓库的master分支

> mac的更新密码和开机密码一致



### 使用git覆盖本地代码

```shell
git fetch --all
git reset --hard origin/master 
git pull
```





### 视频观看记录

2019年7月12日22点13分——————0003java内存模型，第四节结束