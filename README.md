# Code

```
git status

git add -A  提交所有变化  
git add -u  提交被修改(modified)和被删除(deleted)文件，不包括新文件(new)  
git add .  提交新文件(new)和被修改(modified)文件，不包括被删除(deleted)文件  

git commit -a -m "comment"

git push origin master
```

more safer

```
git checkout -b modified
在该分支可以对代码进行修改，删除，增加等。

提交modified这个分支修改，删除、增加的代码：
git commit -a -m 'commit all files'

切换至master分支，把分支modified合并到本地master中。
git checkout master
git merge modified

上传至远程仓库
git push origin master
```
