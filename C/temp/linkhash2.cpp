#include <string.h>
#include <stdio.h>
#include <stdlib.h>
 
typedef struct node{
    char *name;//字段名
    char *desc;//描述
    struct node *next;
}node;
 
#define HASHSIZE 100 //hash表长度
static node* hashtable[HASHSIZE];//定义一个hash数组，该数组的每个元素是一个hash结点指针,并且由于是全局静态变量,默认初始化为NULL
 
unsigned int hash(char *s)
{//哈希函数
    unsigned int h=0;
    for(;*s;s++)
        h=*s+h*31;//将整个字符串按照特定关系转化为一个整数，然后对hash长度取余
    return h%HASHSIZE;
}
 
node* lookup(char *str)
{
    unsigned int hashvalue = hash(str);
    node* np = hashtable[hashvalue];
    for( ; np!=NULL; np = np->next)
    {//这里是链地址法解决的冲突,返回的是第一个链表结点
        if(!strcmp(np->name, str))//strcmp相等的时候才返回0
            return np;
    }
    return NULL;
}
 
char* search(char* name)
{//对hash表查找特定元素(元素是字符串）
    node* np=lookup(name);
    if(np==NULL)
        return NULL;
    else
        return np->desc;
}
 
node* malloc_node(char* name, char* desc)
{//在堆上为结点分配内存，并填充结点
    node *np=(node*)malloc(sizeof(node));
    if(np == NULL)
        return NULL;
    np->name = name;
    np->desc = desc;
    np->next = NULL;
    return np;
}
 
int insert(char* name, char* desc)
{
    unsigned int hashvalue;
    hashvalue = hash(name);
    //头插法，不管该hash位置有没有其他结点，直接插入结点
    node* np = malloc_node(name, desc);
    if (np == NULL) return 0;//分配结点没有成功，则直接返回
    np->next = hashtable[hashvalue];
    hashtable[hashvalue] = np;
    return 1;
}


int main()
{
    char* names[]={"First Name","Last Name","address","phone","k101","k110"};
    char* descs[]={"Kobe","Bryant","USA","26300788","Value1","Value2"};
    
    for(int i=0; i < 6; ++i)
        insert(names[i], descs[i]);
    printf("we should see %s\n",search("k110"));
    insert("phone","9433120451");//这里计算的hash是冲突的，为了测试冲突情况下的插入
    printf("we have %s and %s\n",search("k101"),search("phone"));

    return 0;
}