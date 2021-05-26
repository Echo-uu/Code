#include<iostream>
#include<cmath>
#include<algorithm>
#include<unordered_map>
#include<vector>

using namespace std;
int ans[1000];

//埃拉托色尼筛
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

int lengthOfLongestSubstring() {
    string s;
    cin>>s;
    int left =0, right = 0, ans = 0;
    unordered_map<char,int> mp;
    cout<<s<<endl;
    while(right<s.length()){
        //cout<<"b times: "<<mp['b']<<endl;
        if(mp[s[right]] == 0){
            mp[s[right]]++;
            ans = max(ans,right-left+1);
            cout<<left<<endl<<right<<endl<<ans<<endl<<endl;
            right++;
        }
        else {
            left++;
            if(s[left]==s[right])left++;
            mp[s[right]]--;      
        }
    }
    cout<<ans<<endl;
}

void vectest(){
    vector<int> tmp;
    int i=0;
    while(i<10)tmp.push_back(i++);
    i=0;
    while(i<10)cout<<tmp[i++]<<endl;
    cout<<tmp.size();
}

int main(){
    //vectest();
    //lengthOfLongestSubstring();
    // int *show;
    // show = eratoshenes(1000);
    // for(int i=0; i<1000; i++)
    //     if(*(show + i) != 0)
    //         cout<<*(show + i)<<' '; 
    vector<int> nums1,nums2;
    nums1.push_back(1);
    nums1.push_back(2);
    nums2.push_back(3);
    nums2.push_back(4);
    int s = nums1.size()+nums2.size();
        int cur = -1;int i = 0, j = 0;
        vector<int> tmp;
        while(cur<s/2){
            if(i>=nums1.size())tmp.push_back(nums2[j++]);
            else if(j>=nums2.size())tmp.push_back(nums1[i++]);
            else if(nums1[i]<nums2[j])
                tmp.push_back(nums1[i++]);
            else tmp.push_back(nums2[j++]);
            cur++;cout<<tmp[cur]<<endl;
        }
        cout<<"size: "<<s<<endl<<"cur: "<<cur<<endl;
        for(int i=0;i<=cur;i++)cout<<tmp[i]<<' ';
        cout<<endl;
        if(s%2==0)cout<< double((tmp[cur-1]+tmp[cur])/2.00);
        else cout<< tmp[cur];

    system("pause");
    return 0;
}

