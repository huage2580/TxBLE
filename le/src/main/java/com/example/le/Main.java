package com.example.le;


public class Main {
    static int[] random_tab = {0x67, 0x41, 0x90, 0x15, 0x23, 0x62, 0x54,
            0x49, 0x02, 0x06, 0x93, 0x25, 0x55, 0x49, 0x06, 0x9c, 0x78, 0x26, 0x74,
            0x41, 0x76, 0x43, 0x35, 0x32, 0x07, 0x59, 0x86, 0x92, 0x06, 0x15, 0x9a,
            0x25, 0x32, 0x19, 0x10, 0x39, 0x41, 0x50, 0x09, 0x16, 0x16, 0x2a, 0x87,
            0x51, 0x38, 0x88, 0x43, 0x13, 0x45, 0x72, 0x14, 0xa5, 0x28, 0x16, 0x08,
            0x99, 0xe7, 0x64, 0x62, 0x80, 0x58, 0x20, 0x57, 0x37, 0x74, 0x93, 0x17,
            0x80, 0x38, 0x4e, 0xf7, 0xa7, 0x73, 0x11, 0x99, 0x43, 0x52, 0x38, 0x78,
            0x21, 0x54, 0x32, 0x82, 0x40, 0x74, 0xd7, 0x94, 0x66, 0x61, 0x71, 0x48,
            0x87, 0x17, 0x45, 0x2f, 0x07, 0xe4, 0x18, 0x43, 0x76, 0x96, 0x49, 0x86,
            0x55, 0x22, 0x20, 0x68, 0x08, 0x74, 0x28, 0x23, 0x29, 0x04, 0x70, 0x61,
            0x78, 0x89, 0x70, 0x52, 0x36, 0x26, 0x04, 0x13, 0x70, 0x60, 0x50, 0x24,
            0x72, 0x38, 0x69, 0x83, 0xd5, 0xc5, 0x38, 0x85, 0x58, 0x51, 0x23, 0x22,
            0x91, 0x13, 0x54, 0x24, 0x25, 0x05, 0x89, 0x26, 0x95, 0x80, 0x83, 0x75,
            0x71, 0x6f, 0x62, 0xc7, 0x55, 0x03, 0x30, 0x03, 0x86, 0x97, 0x11, 0x78,
            0x69, 0x79, 0x79, 0x06, 0x98, 0x73, 0x35, 0x29, 0x06, 0x91, 0x56, 0x12,
            0x23, 0x23, 0x04, 0x34, 0xd9, 0x70, 0x34, 0x62, 0x30, 0x91, 0x07, 0x09,
            0x56, 0x42, 0x03, 0x55, 0x48, 0x32, 0x88, 0x65, 0x68, 0x80, 0x00, 0x66,
            0x49, 0x22, 0x70, 0x90, 0x18, 0x88, 0x22, 0x10, 0x49, 0xb7, 0x33, 0x08,
            0x69, 0x09, 0x12, 0x32, 0x93, 0x06, 0x22, 0x97, 0x71, 0x78, 0x47, 0x21,
            0x29, 0x81, 0x87, 0x77, 0x79, 0xc9, 0x86, 0x85, 0x90, 0x84, 0xb7, 0x83,
            0x19, 0x21, 0x21, 0x49, 0x16, 0x41, 0x82, 0x06, 0x87, 0x49, 0x22, 0x16,
            0x24, 0x06, 0x16, 0x20, 0x02, 0x31, 0x13, 0x03, 0x92};
    //mac地址
    static byte[] btaddr={(byte) 0x86,0x55,0x08,0x00,0x00,0x00};
//    static byte[] btaddr={(byte) 0x86,0x55,0x09, (byte) 0xc0,0x04, (byte) 0x82};

    public static void main(String[] args){
        byte[] source = {(byte) 0xe3, (byte) 0xaa, (byte) 0xc8,0x13 , (byte) 0xce,0x36 , (byte) 0x85,0x06 , (byte) 0xd8,0x3b ,0x17 , (byte) 0xbf, (byte) 0xfd,0x74 , (byte) 0xf5,0x7c};
//        byte[] source = {(byte) 0xcc, (byte) 0x93,0x16 ,0x67 ,0x1d ,0x52 , (byte) 0xb3, (byte) 0xb0,0x01 , (byte) 0x91, (byte) 0xa6,0x68 ,0x0f ,0x25 , (byte) 0xc4, (byte) 0xea};
        System.out.println(hex2String(decodeData(source)));
    }


    public static byte[] decodeData(byte[] in){
        int len = in.length;
        int ra =((in[len-1]-btaddr[5])^btaddr[4]) & 0xff;
        int rb = ((in[len-2]-btaddr[5])^btaddr[4]) & 0xff;
        byte[] out = new byte[len];
        for (int i=0;i<len;i++){
            if (i<len-4){
                out[i] = (byte) (((in[i])-random_tab[rb%random_tab.length])^random_tab[ra%random_tab.length]);
                ra++;
                rb++;
            }else {
                out[i]=in[i];
            }
        }
        return out;
    }
    private static String hex2String(byte[] bs){
        StringBuilder sb=new StringBuilder();
        for(byte b:bs){
            String s=Integer.toHexString(b & 0xff);
            sb.append(s.length()==1?"0"+s:s);
            sb.append(" ");
        }
        return sb.toString();
    }
    private static String hex2String(int[] bs){
        StringBuilder sb=new StringBuilder();
        for(int b:bs){
            sb.append(b);
            sb.append(",");
        }
        return sb.toString();
    }
}
