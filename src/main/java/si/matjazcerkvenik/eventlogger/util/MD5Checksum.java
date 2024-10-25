/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.eventlogger.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum {

    /**
     * Return MD5 checksum of a string.
     * @param s
     * @return checksum
     */
    public static String getMd5Checksum(String s) {

        StringBuffer sb = new StringBuffer("");

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] dataBytes = s.getBytes();

            md.update(dataBytes, 0, dataBytes.length);

            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }

}
