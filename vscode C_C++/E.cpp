#include<iostream>
#include<cmath>
#include<algorithm>

using namespace std;
int ans[1000];
int * eratoshenes(int n){
    int tmp;
    for(int i = 1; i <= n; i++)
        ans[i] = i;
    for(int j = 2; j <= (int)sqrt(n); j++){
        if(ans[j] != 0){
            tmp = j * j;
            while(tmp < n){
                ans[tmp] = 0;
                tmp += j;
            }
        }
    }
    // for(int i = 1; i <= n; i++)
    //     cout<<ans[i]<<' ';
    return ans;
}

int gcd1 (int m, int n){
    while(n != 0){
        int r = m % n;
        m = n, n = r;
    }
    return m;
}

int gcd2(int m, int n){
    int t = min(m, n);
    while(m % t != 0 || n % t != 0)
        t--;
    return t;
}



int main(){
    int *show;
    show = eratoshenes(1000);
    for(int i=0; i<1000; i++)
        if(*(show + i) != 0)
            cout<<*(show + i)<<' '; 
    system("pause");
    return 0;
}

