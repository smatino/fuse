/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.fabric.stream.log;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class LogStreamConsumer {
    
    String broker;
    String source;
    String target;
    boolean uncompress = false;
    
    public static void main(String[] args) throws Exception {
        LogStreamConsumer producer = new LogStreamConsumer();
        
        // Process the arguments
        LinkedList<String> argl = new LinkedList<String>(Arrays.asList(args));
        while(!argl.isEmpty()) {
            try {
                String arg = argl.removeFirst();
                if( "--help".equals(arg) ) {
                    displayHelpAndExit(0);
                } else if( "--broker".equals(arg) ) {
                    producer.broker = shift(argl);
                } else if( "--source".equals(arg) ) {
                    producer.source = shift(argl);
                } else if( "--target".equals(arg) ) {
                    producer.target = shift(argl);
                } else if( "--uncompress".equals(arg) ) {
                    producer.uncompress = Boolean.parseBoolean(shift(argl));
                } else {
                    System.err.println("Invalid usage: unknown option: "+arg);
                    displayHelpAndExit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid usage: argument not a number");
                displayHelpAndExit(1);
            }
        }
        if( producer.broker==null ) {
            System.err.println("Invalid usage: --broker option not specified.");
            displayHelpAndExit(1);
        }
        if( producer.source ==null ) {
            System.err.println("Invalid usage: --destination option not specified.");
            displayHelpAndExit(1);
        }

        producer.execute();
        System.exit(0);
    }

    private static String shift(LinkedList<String> argl) {
        if(argl.isEmpty()) {
            System.err.println("Invalid usage: Missing argument");
            displayHelpAndExit(1);
        }
        return argl.removeFirst();
    }

    private static void displayHelpAndExit(int exitCode) {
        System.exit(exitCode);
    }

    private void execute() throws Exception {
        CamelContext context = new DefaultCamelContext();
        // no need to use JMX for this embedded CamelContext
        context.disableJMX();
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent(broker));
        
        context.addRoutes(new RouteBuilder() {
            public void configure() throws Exception {
                RouteDefinition route = from("activemq:"+ source);
                if(uncompress) {
                    route = route.process(new SnappyDecompressor());
                }
                route.to(target);
            }
        });
        context.start();

        // block until done.
        synchronized (this) {
            while(true) {
                this.wait();
            }
        }
    }

}