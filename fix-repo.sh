#!/bin/sh
 
git filter-branch -f --env-filter '
 
aname="$GIT_AUTHOR_NAME"
cname="$GIT_COMMITTER_NAME"
 
amail="$GIT_AUTHOR_EMAIL"
cmail="$GIT_COMMITTER_EMAIL"
 
 
if [ "$GIT_COMMITTER_NAME" = "Gordan Valjak" ]
then
    cname="Gordan Valjak"
    cmail="gordan@element.hr"
fi
if [ "$GIT_AUTHOR_NAME" = "Gordan Valjak" ]
then
    aname="Gordan Valjak"
    amail="gordan@element.hr"
fi
 
export GIT_AUTHOR_EMAIL="$amail"
export GIT_COMMITTER_EMAIL="$cmail"
export GIT_AUTHOR_NAME="$aname"
export GIT_COMMITTER_NAME="$cname"
 
'


