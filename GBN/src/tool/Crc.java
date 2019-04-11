package tool;

import java.util.Map;

import gbn.GBNClient;

import java.lang.*;
import java.io.*;
public class Crc
{
	String path = ".\\bin\\crc.ini";
    public static String s  ="10001000000100001" ;
	public static  StringBuffer poly =new StringBuffer(s);
	public Crc() throws IOException {
		//ReadIniFile rf = new ReadIniFile(path);
		//Map<String, String> map = rf.readFile();
		//s = map.get("crc");
		//poly = new StringBuffer(s);
		
	}
	
	 
     public static char XOR(char a,char b)
     {
         return a==b?'0':'1';
     }
     public static String crc_remainder(StringBuffer input_bitstring)//crc码的生成程序
     {
         int len_input = input_bitstring.length();
         int len_poly = poly.length();
         StringBuffer padding=new StringBuffer("0");
         for(int i=1;i<len_poly-1;i++)
         {
             padding=padding.append("0");
         }

          StringBuffer padded_input = input_bitstring.append(padding);

          for(int j=0;j<len_input;++j)
          {
              if(padded_input.charAt(j)=='0')
              {
                   continue;
              }
              for(int k=j;k<j+len_poly;++k)
              {
                  padded_input.setCharAt(k,XOR(padded_input.charAt(k),poly.charAt(k-j)));
              }

          }
          return padded_input.substring(len_input,len_input+len_poly-1);
     }
  public static String crc_check(StringBuffer input_bitstring)//crc码的生成程序
  {

      int len_poly = poly.length();
      int len_input = input_bitstring.length()-len_poly+1;
      StringBuffer padded_input = input_bitstring;

      for(int j=0;j<len_input;++j)
      {
          if(padded_input.charAt(j)=='0')
          {
              continue;
          }
          for(int k=j;k<j+len_poly;++k)
          {
              padded_input.setCharAt(k,XOR(padded_input.charAt(k),poly.charAt(k-j)));
          }

      }
      return padded_input.toString();
  }
    
     public static void main(String args[])throws Exception
     {
         String a = "101";
         StringBuffer sb = new StringBuffer(a);
         String re = Crc.crc_remainder(sb);
         String zz = a + re;
         StringBuffer sz = new StringBuffer(zz);
         StringBuffer ss = new StringBuffer(a+re);
         
         String check_ss = Crc.crc_check(ss);
         String check_sz = Crc.crc_check(ss);
         
         System.out.println("remain ==" + re);
         System.out.println("checkss ==" + check_ss);
         System.out.println("checksz ==" + check_sz);
     }
}
