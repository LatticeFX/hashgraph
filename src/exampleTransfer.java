import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.crypto.ed25519.*;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Client;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Duration;
import java.util.Map;
import com.hedera.hashgraph.sdk.account.AccountId;

public class exampleTransfer {
    public static void main(String[] args) throws HederaException, InterruptedException {
        //Connect to client
        // load the environment values from env file
        Dotenv dotenv = Dotenv.configure().filename(".env").ignoreIfMissing().load();
        var client = new Client(Map.of(AccountId.fromString(
                dotenv.get("NODE_ID"))
                , dotenv.get("NODE_ADDRESS")
        )
        );
        client.setOperator(AccountId.fromString(dotenv.get("OPERATOR_ID")), Ed25519PrivateKey.fromString(dotenv.get("OPERATOR_KEY")));

        // Generate a Ed25519 private, public key pair
        var newKey = Ed25519PrivateKey.generate();
        var newPublicKey = newKey.getPublicKey();

        System.out.println("Lina's private key = " + newKey);
        System.out.println("Lina's public key = " + newPublicKey);

        var newAccountId =  new AccountCreateTransaction(client)
                .setTransactionFee(50000000)
                .setAutoRenewPeriod(Duration.ofSeconds(7890000))
                .setInitialBalance(5000)
                .setKey(newPublicKey)
                .executeForReceipt();

        var receiptAccountId = newAccountId.getAccountId();

        System.out.println("Lina's account id = " + receiptAccountId);

        //Check balance for the new account
        var balance = client.getAccountBalance(receiptAccountId);
        System.out.println("initial balance = " + balance);

        // Transfer from the operator to the new account without a receipt
        client.transferCryptoTo(receiptAccountId, 10_000);
        // wait a few seconds for the transaction to complete
        Thread.sleep(5000);
        balance = client.getAccountBalance(receiptAccountId);
        System.out.println("new balance = " + balance);

    }
}