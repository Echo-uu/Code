#include<iostream>
#include<string>

using namespace std;

struct node{
    char c;
    int cnt =0;
}n[100], t[100];

int main(){
    string name, typed;
    cin>>name;
    cin>>typed;
    int x = 0;
    name = name + '0', typed = typed + '0';
    for(int i=0; i<name.length()-1; i++){
        if(name[i] == name[i+1])
            n[x].c = name[i], n[x].cnt++;
        else n[x].c = name[i], n[x++].cnt++;
    }
    x = 0;
    for(int i=0; i<typed.length()-1; i++){
        if(typed[i] == typed[i+1])
            t[x].c = typed[i], t[x].cnt++;
        else t[x].c = typed[i], t[x++].cnt++;
    }
    for(int i=0; i<=x; i++){
        if(t[i].c == n[i].c && t[i].cnt >= n[i].cnt)
            continue;
        else{
            cout<< "false";
            system("pause");
        }
    }
    cout<<"true";
}