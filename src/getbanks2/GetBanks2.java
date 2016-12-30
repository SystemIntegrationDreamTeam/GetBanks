/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package getbanks2;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Buhrkall
 */
public class GetBanks2 {

    private final static String LISTENING_QUEUE_NAME = "RuleBaseQueue";
    private final static String SENDING_QUEUE_NAME = "RecipListQueue";

    private static String message;

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */
    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setVirtualHost("student");
        factory.setUsername("Dreamteam");
        factory.setPassword("bastian");
        Connection connection = factory.newConnection();
        Channel listeningChannel = connection.createChannel();
        Channel sendingChannel = connection.createChannel();

        listeningChannel.queueDeclare(LISTENING_QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(listeningChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");

                String[] arr = message.split(",");

                String banks = getBanks(arr[0], Integer.parseInt(arr[1]), Double.parseDouble(arr[2]), Integer.parseInt(arr[3]));

                message += "," + banks;

                sendingChannel.queueDeclare(SENDING_QUEUE_NAME, false, false, false, null);
                sendingChannel.basicPublish("", SENDING_QUEUE_NAME, null, message.getBytes());

            }
        };
        listeningChannel.basicConsume(LISTENING_QUEUE_NAME, true, consumer);

    }

    private static String getBanks(java.lang.String ssn, int creditScore, double loanAmount, int loanDuration) {
        com.mycompany.rulebase.RuleBase_Service service = new com.mycompany.rulebase.RuleBase_Service();
        com.mycompany.rulebase.RuleBase port = service.getRuleBasePort();
        return port.getBanks(ssn, creditScore, loanAmount, loanDuration);
    }

}
