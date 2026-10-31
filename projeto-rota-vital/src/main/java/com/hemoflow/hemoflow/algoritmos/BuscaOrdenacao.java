package com.hemoflow.hemoflow.algoritmos;

import java.util.Arrays;

/** Implementações didáticas próprias de busca e ordenação para a U2. */
public final class BuscaOrdenacao {
    private BuscaOrdenacao() {}
    public static int sequencial(int[] v,int chave){for(int i=0;i<v.length;i++)if(v[i]==chave)return i;return -1;}
    /** Requer vetor crescente. */
    public static int binaria(int[] v,int chave){int l=0,r=v.length-1;while(l<=r){int m=l+(r-l)/2;if(v[m]==chave)return m;if(v[m]<chave)l=m+1;else r=m-1;}return -1;}
    public static void bubble(int[] v){for(int i=0;i<v.length-1;i++){boolean trocou=false;for(int j=0;j<v.length-i-1;j++)if(v[j]>v[j+1]){int t=v[j];v[j]=v[j+1];v[j+1]=t;trocou=true;}if(!trocou)break;}}
    public static void selection(int[] v){for(int i=0;i<v.length-1;i++){int m=i;for(int j=i+1;j<v.length;j++)if(v[j]<v[m])m=j;int t=v[i];v[i]=v[m];v[m]=t;}}
    public static void insertion(int[] v){for(int i=1;i<v.length;i++){int x=v[i],j=i-1;while(j>=0&&v[j]>x){v[j+1]=v[j];j--;}v[j+1]=x;}}
    public static void merge(int[] v){if(v.length>1)mergeRec(v,0,v.length-1);}
    private static void mergeRec(int[]v,int l,int r){if(l>=r)return;int m=l+(r-l)/2;mergeRec(v,l,m);mergeRec(v,m+1,r);int[] a=Arrays.copyOfRange(v,l,m+1),b=Arrays.copyOfRange(v,m+1,r+1);int i=0,j=0,k=l;while(i<a.length&&j<b.length)v[k++]=a[i]<=b[j]?a[i++]:b[j++];while(i<a.length)v[k++]=a[i++];while(j<b.length)v[k++]=b[j++];}
}
