#include<bits/stdc++.h>

using namespace std;

bool work(string s){
    int n = s.size();
    if (n % 2 == 1) {
        return false;
    }

    unordered_map<char, char> pairs = {
        {')', '('},
        {']', '['},
        {'}', '{'}
    };
    stack<char> stk;
    for (char ch: s) {
        if (pairs.count(ch)) {
            if (stk.empty() || stk.top() != pairs[ch]) {
                return false;
            }
            stk.pop();
        }
        else {
            stk.push(ch);
        }
    }
    return stk.empty();
}

int main(){
    string s;
    // cin>>s;

    unordered_map<char, char> pairs = {
        {')', '('},
        {']', '['},
        {'}', '{'}
    };
    cout<<pairs.count(')')<<endl;
    cout<<pairs.count('(');
    //cout<<work(s);
    
}

