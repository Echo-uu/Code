#include<bits/stdc++.h>

using namespace std;

vector<int> p;

class Solution {
public:
    int maxProfit(vector<int>& prices) {
        int earn = 0;
        for(int i=0; i < prices.size()-1; i++){
            if(prices[i]<prices[i+1])
                earn += (prices[i+1] - prices[i]);
        }
        return earn;
    }
};