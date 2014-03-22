package kademlia.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Comparator;
import kademlia.message.Streamable;
import kademlia.routing.RoutingTable;

/**
 * A Node in the Kademlia network
 *
 * @author Joshua Kissoon
 * @since 20140202
 * @version 0.1
 */
public class Node implements Streamable
{

    private NodeId nodeId;
    private InetAddress inetAddress;
    private int port;
    private String strRep;

    private transient RoutingTable routingTable;

    
    {
        this.routingTable = new RoutingTable(this);
    }

    public Node(NodeId nid, InetAddress ip, int port)
    {
        this.nodeId = nid;
        this.inetAddress = ip;
        this.port = port;
        this.strRep = this.nodeId.toString();
    }

    /**
     * Load the Node's data from a DataInput stream
     *
     * @param in
     *
     * @throws IOException
     */
    public Node(DataInputStream in) throws IOException
    {
        this.fromStream(in);
        this.strRep = this.nodeId.toString();
    }

    /**
     * Set the InetAddress of this node
     *
     * @param addr The new InetAddress of this node
     */
    public void setInetAddress(InetAddress addr)
    {
        this.inetAddress = addr;
    }

    /**
     * @return The NodeId object of this node
     */
    public NodeId getNodeId()
    {
        return this.nodeId;
    }

    /**
     * Creates a SocketAddress for this node
     *
     * @return
     */
    public SocketAddress getSocketAddress()
    {
        return new InetSocketAddress(this.inetAddress, this.port);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        /* Add the NodeId to the stream */
        this.nodeId.toStream(out);

        /* Add the Node's IP address to the stream */
        byte[] a = inetAddress.getAddress();
        if (a.length != 4)
        {
            throw new RuntimeException("Expected InetAddress of 4 bytes, got " + a.length);
        }
        out.write(a);

        /* Add the port to the stream */
        out.writeInt(port);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        /* Load the NodeId */
        this.nodeId = new NodeId(in);

        /* Load the IP Address */
        byte[] ip = new byte[4];
        in.readFully(ip);
        this.inetAddress = InetAddress.getByAddress(ip);

        /* Read in the port */
        this.port = in.readInt();
    }

    /**
     * @return The RoutingTable of this Node
     */
    public RoutingTable getRoutingTable()
    {
        return this.routingTable;
    }

    /**
     * Sets a new routing table to this node, mainly used when we retrieve the node from a saved state
     *
     * @param tbl The routing table to use
     */
    public void setRoutingTable(RoutingTable tbl)
    {
        this.routingTable = tbl;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Node)
        {
            Node n = (Node) o;
            if (o == this)
            {
                return true;
            }
            return this.getNodeId().equals(((Node) o).getNodeId());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getNodeId().hashCode();
    }

    @Override
    public String toString()
    {
        return this.getNodeId().toString();
    }

    /**
     * A DistanceComparator is used to compare Node objects based on their closeness
     * */
    public static class DistanceComparator implements Comparator
    {

        private final NodeId nid;

        /**
         * The NodeId relative to which the distance should be measured.
         *
         * @param nid
         * */
        public DistanceComparator(NodeId nid)
        {
            this.nid = nid;
        }

        /**
         * Compare two objects which must both be of type <code>Node</code>
         * and determine which is closest to the identifier specified in the
         * constructor.
         * */
        @Override
        public int compare(Object o1, Object o2)
        {
            Node n1 = (Node) o1;
            Node n2 = (Node) o2;
//            System.out.println("\nDistance Comparator: " + nodeId);
//            System.out.println("Distance Comparator: " + n1.getNodeId());
//            System.out.println("Distance Comparator: " + n2.getNodeId());

            /* Check if they are equal and return 0 */
//            if (n1.equals(n2))
//            {
//                //System.out.println("Distance Comparator: Return 0");
//                return 0;
//            }
            //System.out.println("\n **************** Compare Starting **************** ");
            //System.out.println("Comparing to: " + this.nodeId);
            int distance1 = nid.getDistance(n1.getNodeId());
            //System.out.println("Node " + n1.getNodeId() + " distance: " + index1);
            int distance2 = nid.getDistance(n2.getNodeId());
            //System.out.println("Node " + n2.getNodeId() + " distance: " + index2);

            int retval;

            if ((distance1 == distance2) && n1.equals(n2))
            {
                /**
                 * If the first node is farther to the given node, return 1
                 *
                 * @note -1 will also be returned if both nodes are the same distance away
                 * This really don't make a difference though, since they need to be sorted.
                 */
                //System.out.println("Distance Comparator: Return -1");
                retval = 0;
            }
            else if (distance1 < distance2)
            {
                /* If the first node is closer to the given node, return 1 */
                //System.out.println("Distance Comparator: Return 1");
                retval = 1;
            }
            else
            {
                return -1;
            }

            //System.out.println("Returned: " + retval);
            //System.out.println("**************** Compare Ended ***************** \n");
            return retval;
        }
    }
}
