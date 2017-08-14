import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

public class HelloWorld {

    public static void main(String []args)  throws NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Hello World");
        
        UTXOPool pool = createUTXOPool();
        
        TxHandler txHandler = new TxHandler(pool);
        
    }
    
    private static UTXOPool createUTXOPool() throws NoSuchAlgorithmException, InvalidKeySpecException {
        PublicKey scroogePublicKey = getPublicKey(hexStringToByteArray("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"));
        
         UTXOPool pool = new UTXOPool();
         
         byte[] txHash = hexStringToByteArray("111");
         Transaction tx = new Transaction();
         tx.addOutput(10, scroogePublicKey);
         pool.addUTXO(new UTXO(txHash, 0), tx.getOutput(0));
         return pool;
    }
     
    private static PublicKey getPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
        return factory.generatePublic(encodedKeySpec);
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
