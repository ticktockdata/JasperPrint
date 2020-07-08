
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Feb 07, 2019
 */
public class IPScanner {

    private AtomicInteger ct = new AtomicInteger(0);

    public IPScanner() {

    }

    /**
     * This only works via IPv4 Address - IPv6 not implemented
     *
     * @param port
     * @return
     */
    public InetAddress[] getAddressesWithLivePort(final int port) {

        final ArrayBlockingQueue<InetAddress> addrs = new java.util.concurrent.ArrayBlockingQueue<InetAddress>(100);

        try {

            for (InetAddress scanAddr : getLiveAddresses()) {
                try {
                    Socket soc = new Socket(scanAddr, port);
                    addrs.add(scanAddr);
                    soc.close();
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
        }

        return addrs.toArray(new InetAddress[addrs.size()]);
    }

    /**
     * Finds all live hosts with the same xx.xx.xx. address as this machine.
     *
     * @return
     */
    public InetAddress[] getLiveAddresses() {

        final ArrayBlockingQueue<InetAddress> addrs = new java.util.concurrent.ArrayBlockingQueue<InetAddress>(100);

        try {
            InetAddress[] localAddresses = java.net.InetAddress.getAllByName(null);
            for (InetAddress a : localAddresses) {
                System.out.println(a.getHostAddress());
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {

                    System.out.println(ni.getDisplayName() + " : " + ia.getAddress().getHostAddress());
                    InetAddress addr = ia.getBroadcast();
                    if (addr != null) {
                        System.out.println("Broadcast: " + ia.getAddress().getCanonicalHostName());
                    }
                    addr = ia.getAddress();
                    byte[] a = addr.getAddress();
                    int count = 0;
                    if (a.length == 4 && a[0] != 127) {
                        System.out.println(" $$$$$$$$$$ starting scan $$$$$$$$$$$$$");
                        byte thisNum = a[3];
                        String s = a[0] + "." + a[1] + "." + a[2] + ".";
                        for (int i = 1; i < 255; i++) {
                            if (i != thisNum) {
                                final String ipa = s + i;
                                //System.out.println(" >>>>>>> Scanning " + i);
                                Thread t = new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            InetAddress scanAddr = InetAddress.getByName(ipa);
                                            if (scanAddr.isReachable(5000)) {
                                                addrs.add(scanAddr);
                                            }
                                        } catch (Exception ex) {
                                            Logger.getLogger(IPScanner.class.getName()).log(Level.SEVERE, null, ex);
                                        } finally {
                                            ct.incrementAndGet();
                                            //System.out.println(ct);
                                        }

                                    }
                                };
                                t.start();
                                count++;
                            }
                        }
                        System.out.println(" ######## waiting to finish #######");
                        while (ct.intValue() < count) {
                            Thread.sleep(50);
                        }
                        ct.set(0); // ready for next
                        System.out.println(" ********* Next Loop ************");
                    }
                }

            }
        } catch (Exception ex) {
        }

        return addrs.toArray(new InetAddress[addrs.size()]);

    }

    public static void main(String[] args) {
        IPScanner ips = new IPScanner();

        int port = 80;  //9091;
        InetAddress[] a = ips.getAddressesWithLivePort(port);

        System.out.println("--------------------- The following IP's were found");
        for (InetAddress ia : a) {
            System.out.println(ia.getHostAddress());
        }
        System.out.println("--------------------- End of list");
    }

}
