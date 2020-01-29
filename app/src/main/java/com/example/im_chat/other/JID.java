// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 2013/10/28 16:11:52
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   JID.java

package com.example.im_chat.other;

import java.io.Serializable;


@SuppressWarnings({ "rawtypes", "serial" })
public class JID
        implements Comparable, Serializable
{

    public static String escapeNode(String node)
    {
        if(node == null)
            return null;
        StringBuilder buf = new StringBuilder(node.length() + 8);
        int i = 0;
        for(int n = node.length(); i < n; i++)
        {
            char c = node.charAt(i);
            switch(c)
            {
                case 34: // '"'
                    buf.append("\\22");
                    break;

                case 38: // '&'
                    buf.append("\\26");
                    break;

                case 39: // '\''
                    buf.append("\\27");
                    break;

                case 47: // '/'
                    buf.append("\\2f");
                    break;

                case 58: // ':'
                    buf.append("\\3a");
                    break;

                case 60: // '<'
                    buf.append("\\3c");
                    break;

                case 62: // '>'
                    buf.append("\\3e");
                    break;

                case 64: // '@'
                    buf.append("\\40");
                    break;

                case 92: // '\\'
                    int c2 = i + 1 >= n ? -1 : ((int) (node.charAt(i + 1)));
                    int c3 = i + 2 >= n ? -1 : ((int) (node.charAt(i + 2)));
                    if(c2 == 50 && (c3 == 48 || c3 == 50 || c3 == 54 || c3 == 55 || c3 == 102) || c2 == 51 && (c3 == 97 || c3 == 99 || c3 == 101) || c2 == 52 && c3 == 48 || c2 == 53 && c3 == 99)
                        buf.append("\\5c");
                    else
                        buf.append(c);
                    break;

                default:
                    if(Character.isWhitespace(c))
                        buf.append("\\20");
                    else
                        buf.append(c);
                    break;
            }
        }

        return buf.toString();
    }

    public static String unescapeNode(String node)
    {
        if(node == null)
            return null;
        char nodeChars[] = node.toCharArray();
        StringBuilder buf = new StringBuilder(nodeChars.length);
        int i = 0;
        for(int n = nodeChars.length; i < n; i++)
        {
            char c = node.charAt(i);
            if(c == '\\' && i + 2 < n)
            {
                char c2 = nodeChars[i + 1];
                char c3 = nodeChars[i + 2];
                if(c2 == '2')
                    switch(c3)
                    {
                        case 48: // '0'
                            buf.append('\n');
                            i += 2;
                            continue;

                        case 50: // '2'
                            buf.append('"');
                            i += 2;
                            continue;

                        case 54: // '6'
                            buf.append('&');
                            i += 2;
                            continue;

                        case 55: // '7'
                            buf.append('\'');
                            i += 2;
                            continue;

                        case 102: // 'f'
                            buf.append('/');
                            i += 2;
                            continue;
                    }
                else
                if(c2 == '3')
                    switch(c3)
                    {
                        case 97: // 'a'
                            buf.append(':');
                            i += 2;
                            continue;

                        case 99: // 'c'
                            buf.append('<');
                            i += 2;
                            continue;

                        case 101: // 'e'
                            buf.append('>');
                            i += 2;
                            continue;
                    }
                else
                if(c2 == '4')
                {
                    if(c3 == '0')
                    {
                        buf.append("@");
                        i += 2;
                        continue;
                    }
                } else
                if(c2 == '5' && c3 == 'c')
                {
                    buf.append("\\");
                    i += 2;
                    continue;
                }
            }
            buf.append(c);
        }

        return buf.toString();
    }

    @Override
    public int compareTo(Object arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static void main(String[] arg){
        String mst = escapeNode("123@" +
                "321");
        System.out.println(escapeNode("123@321"));
        System.out.println(unescapeNode("123\40321"));
    }


}