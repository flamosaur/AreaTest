import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class AreaItem {
    double cx, cy, cz; 
    int w, h, d, radius, length;
    Color color;
    String type; 
    double pitch, yaw;

    public AreaItem(double cx, double cy, double cz, int w, int h, int d, String type, Color col, double p, double y) {
        this.cx = cx; this.cy = cy; this.cz = cz;
        this.w = w; this.h = h; this.d = d;
        this.radius = w; this.length = h;
        this.type = type; this.color = col;
        this.pitch = Math.toRadians(p);
        this.yaw = Math.toRadians(y);
    }

    public AreaItem(double cx, double cy, double cz, int r, int len, String type, Color col, double p, double y) {
        this(cx, cy, cz, r, len, r, type, col, p, y);
    }
}

public class Area extends JPanel implements ActionListener, KeyListener {
    double rotateX = 0.2, rotateY = 0; 
    double offX = 0, offY = -50, offZ = -200; 
    
    javax.swing.Timer timer;
    boolean up, down, left, right, wKey, aKey, sKey, dKey, pageUpKey, pageDownKey, tKey, fKey, gKey, hKey;
    
    boolean isWalking = false;
    double animTime = 0;
    int num = 0;
    
    ArrayList<AreaItem> items = new ArrayList<>();

    class Point3D { double x, y, z; Point3D(double x, double y, double z) { this.x=x; this.y=y; this.z=z; } }

    class Face {
        Polygon poly; double depth; Color col; boolean isLine = false; Point p1, p2;
        Face(Polygon p, double d, Color c) { this.poly = p; this.depth = d; this.col = c; }
        Face(Point p1, Point p2, double d, Color c) { 
            this.p1 = p1; this.p2 = p2; this.depth = d; this.col = c; this.isLine = true; 
        }
        void draw(Graphics2D g2d) {
            g2d.setColor(col);
            if(isLine) { g2d.drawLine(p1.x, p1.y, p2.x, p2.y); } 
            else { g2d.fill(poly); g2d.setColor(col.darker()); g2d.draw(poly); }
        }
    }

    class SphereFace extends Face {
        int sx, sy, size;
        SphereFace(int x, int y, int s, double d, Color c) {
            super(null, d, c); this.sx = x; this.sy = y; this.size = s;
        }
        @Override void draw(Graphics2D g2d) {
            g2d.setColor(col); g2d.fillOval(sx - size/2, sy - size/2, size, size);
            g2d.setColor(Color.BLACK); g2d.drawOval(sx - size/2, sy - size/2, size, size);
        }
    }

    public Area() {
        Color vhsYellow = new Color(240, 200, 0);
        // KEY For Prism and Sphere: (X pos, Y pos, Z pos, Width, Height, Depth, Type, Color, Pitch, Yaw)
        // KEY: (X pos, Y pos, Z pos, Radius, Height, Type, Color, Pitch, Yaw)
        
        //Walls and Ground dont work especially ground
        /*items.add(new AreaItem(500, -10, 400, 1, 500, 1000, "prism", Color.WHITE, 0, 0)); 
        items.add(new AreaItem(0, -10, -600, 1000, 500, 1, "prism", Color.WHITE, 0, 0)); 
        items.add(new AreaItem(0, -10, 600, 1000, 500, 1, "prism", Color.WHITE, 0, 0)); 
        items.add(new AreaItem(-500, -10, 400, 1, 500, 1000, "prism", Color.WHITE, 0, 0));
        items.add(new AreaItem(0, 100, 400, 2000, 1, 2000, "prism", Color.WHITE, 0, 0)); */
        items.add(new AreaItem(0, -100, 400, 45, 45, 45, "sphere", vhsYellow, 0, 0)); 
        items.add(new AreaItem(0, -10, 400, 60, 90, 40, "prism", vhsYellow, 0, 0));  
        items.add(new AreaItem(-45, -10, 375, 8, 100, "cylinder", Color.GRAY, -45, 35));
        items.add(new AreaItem(45, -10, 375, 8, 100, "cylinder", Color.GRAY, -45, -35));
        items.add(new AreaItem(-300, 50, 600, 50, 150, "cone", Color.CYAN, 0, 0));
        items.add(new AreaItem(300, 50, 600, 50, 150, "tri_pyramid", new Color(180, 70, 255), 0, 0));
        items.add(new AreaItem(0, 50, 800, 80, 160, "sq_pyramid", Color.GREEN, 0, 0));
        items.add(new AreaItem(-20, 36, 400, 8, 100, "cylinder", Color.GRAY, 0, 0));
        items.add(new AreaItem(20, 36, 400, 8, 100, "cylinder", Color.GRAY, 0, 0));

        timer = new javax.swing.Timer(16, this);
        timer.start();
        addKeyListener(this);
        setFocusable(true);
        setBackground(new Color(15, 15, 25));
    }

    private Point3D rotatePoint(double x, double y, double z, AreaItem L) {
        double lx = x - L.cx; double ly = y - L.cy; double lz = z - L.cz;
        double ly1 = ly * Math.cos(L.pitch) - lz * Math.sin(L.pitch);
        double lz1 = ly * Math.sin(L.pitch) + lz * Math.cos(L.pitch);
        double lx2 = lx * Math.cos(L.yaw) + lz1 * Math.sin(L.yaw);
        double lz2 = -lx * Math.sin(L.yaw) + lz1 * Math.cos(L.yaw);

        double wx = (lx2 + L.cx) - offX; 
        double wy = (ly1 + L.cy) - offY; 
        double wz = (lz2 + L.cz) - offZ;

        double rx = wx * Math.cos(rotateY) + wz * Math.sin(rotateY);
        double rz1 = -wx * Math.sin(rotateY) + wz * Math.cos(rotateY);
        double ry = wy * Math.cos(rotateX) + rz1 * Math.sin(rotateX);
        double rz = -wy * Math.sin(rotateX) + rz1 * Math.cos(rotateX);

        double safeZ = Math.max(1.0, rz); 
        double zoom = 600.0 / safeZ; 
        return new Point3D(rx * zoom, ry * zoom, rz);
    }

    private Face createFace(Point3D[] verts, Color col) {
        boolean allBehind = true;
        for(Point3D v : verts) {
            if (v.z >= 10) allBehind = false;
        }
        if (allBehind) return null;
        double v1x = verts[1].x - verts[0].x;
        double v1y = verts[1].y - verts[0].y;
        double v2x = verts[2].x - verts[0].x;
        double v2y = verts[2].y - verts[0].y;
        if ((v1x * v2y - v1y * v2x) < 0) return null; 

        Polygon p = new Polygon(); double avgZ = 0;
        for(Point3D v : verts) { p.addPoint((int)v.x, (int)v.y); avgZ += v.z; }
        return new Face(p, avgZ / verts.length, col);
    }

    private ArrayList<Face> getShapeFaces(AreaItem L) {
        ArrayList<Face> faces = new ArrayList<>();
        if (L.type.equals("prism")) {
            double w = L.w/2.0, h = L.h/2.0, d = L.d/2.0;
            Point3D[] v = { 
                rotatePoint(L.cx-w,L.cy-h,L.cz-d,L), rotatePoint(L.cx+w,L.cy-h,L.cz-d,L), 
                rotatePoint(L.cx+w,L.cy+h,L.cz-d,L), rotatePoint(L.cx-w,L.cy+h,L.cz-d,L),
                rotatePoint(L.cx-w,L.cy-h,L.cz+d,L), rotatePoint(L.cx+w,L.cy-h,L.cz+d,L), 
                rotatePoint(L.cx+w,L.cy+h,L.cz+d,L), rotatePoint(L.cx-w,L.cy+h,L.cz+d,L) 
            };
            int[][] fIdx = {{0,1,2,3}, {5,4,7,6}, {4,0,3,7}, {1,5,6,2}, {4,5,1,0}, {3,2,6,7}};
            for(int[] i : fIdx) faces.add(createFace(new Point3D[]{v[i[0]], v[i[1]], v[i[2]], v[i[3]]}, L.color));
        } else if (L.type.equals("cylinder")) {
            int res = 24; 
            double topY = (L.cy == 36) ? 0 : -L.length/2.0;
            double botY = (L.cy == 36) ? L.length : L.length/2.0;

            Point3D tc = rotatePoint(L.cx, L.cy + topY, L.cz, L);
            Point3D bc = rotatePoint(L.cx, L.cy + botY, L.cz, L);
            for(int i=0; i<res; i++) {
                double a1 = i*2*Math.PI/res, a2 = (i+1)*2*Math.PI/res;
                Point3D t1 = rotatePoint(L.cx+Math.cos(a1)*L.radius, L.cy+topY, L.cz+Math.sin(a1)*L.radius, L);
                Point3D t2 = rotatePoint(L.cx+Math.cos(a2)*L.radius, L.cy+topY, L.cz+Math.sin(a2)*L.radius, L);
                Point3D b1 = rotatePoint(L.cx+Math.cos(a1)*L.radius, L.cy+botY, L.cz+Math.sin(a1)*L.radius, L);
                Point3D b2 = rotatePoint(L.cx+Math.cos(a2)*L.radius, L.cy+botY, L.cz+Math.sin(a2)*L.radius, L);
                faces.add(createFace(new Point3D[]{t1, t2, b2, b1}, L.color));
                faces.add(createFace(new Point3D[]{tc, t2, t1}, L.color.brighter()));
                faces.add(createFace(new Point3D[]{bc, b1, b2}, L.color.darker()));
            }
        } else if (L.type.equals("cone") || L.type.contains("pyramid")) {
            int s = L.type.equals("cone") ? 24 : (L.type.equals("sq_pyramid") ? 4 : 3);
            double hh = L.length / 2.0;
            Point3D tip = rotatePoint(L.cx, L.cy + hh, L.cz, L);
            Point3D baseCenter = rotatePoint(L.cx, L.cy - hh, L.cz, L);
            for(int i=0; i<s; i++) {
                double a1 = i*2*Math.PI/s, a2 = (i+1)*2*Math.PI/s;
                Point3D b1 = rotatePoint(L.cx+Math.cos(a1)*L.radius, L.cy+hh, L.cz+Math.sin(a1)*L.radius, L);
                Point3D b2 = rotatePoint(L.cx+Math.cos(a2)*L.radius, L.cy+hh, L.cz+Math.sin(a2)*L.radius, L);
                faces.add(createFace(new Point3D[]{tip, b1, b2}, L.color));
                faces.add(createFace(new Point3D[]{baseCenter, b2, b1}, L.color.darker()));
            }
        }
        return faces;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.translate(getWidth()/2, getHeight()/2);
        ArrayList<Face> allFaces = new ArrayList<>();
        
        for(int i = 0; i < items.size(); i++) {
            AreaItem L = items.get(i);

            if (isWalking && L.type.equals("cylinder") && L.cy == 36) {
                double swing = (i == 7) ? Math.sin(animTime) : Math.sin(animTime + Math.PI);
                L.pitch = Math.toRadians(swing * 30);
            }

            if (L.type.equals("sphere")) {
                Point3D sp = rotatePoint(L.cx, L.cy, L.cz, L);
                if (sp.z > 10) {
                    int size = (int)(L.w * (600.0 / sp.z));
                    allFaces.add(new SphereFace((int)sp.x, (int)sp.y, size, sp.z, L.color));
                }
            } else {
                ArrayList<Face> itemFaces = getShapeFaces(L);
                for(Face f : itemFaces) if(f != null) allFaces.add(f);
            }
        }
        allFaces.sort((f1, f2) -> Double.compare(f2.depth, f1.depth));
        for(Face f : allFaces) f.draw(g2d);
    }

    public void actionPerformed(ActionEvent e) {
        if (isWalking) animTime += 0.15;

        if (up) rotateX += 0.04; if (down) rotateX -= 0.04;
        if (left) rotateY += 0.04; if (right) rotateY -= 0.04;
        double speed = 8.0; double sY = Math.sin(rotateY), cY = Math.cos(rotateY);
        if (wKey) { offX -= sY*speed; offZ += cY*speed; } if (sKey) { offX += sY*speed; offZ -= cY*speed; }
        if (aKey) { offX -= cY*speed; offZ -= sY*speed; } if (dKey) { offX += cY*speed; offZ += sY*speed; }
        
        if (num >= 0 && num < items.size()) {
            AreaItem selected = items.get(num);
            double moveSpeed = 5.0;
            if (tKey) selected.cz += moveSpeed; 
            if (gKey) selected.cz -= moveSpeed;
            if (fKey) selected.cx -= moveSpeed; 
            if (hKey) selected.cx += moveSpeed;
        }

        if (pageUpKey) offY -= speed; if (pageDownKey) offY += speed;
        repaint();
    }

    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_SPACE) isWalking = !isWalking;
        if (k == KeyEvent.VK_UP) up = true; if (k == KeyEvent.VK_DOWN) down = true;
        if (k == KeyEvent.VK_LEFT) left = true; if (k == KeyEvent.VK_RIGHT) right = true;
        if (k == KeyEvent.VK_W) wKey = true; if (k == KeyEvent.VK_S) sKey = true;
        if (k == KeyEvent.VK_A) aKey = true; if (k == KeyEvent.VK_D) dKey = true;
        if (k == KeyEvent.VK_PAGE_UP) pageUpKey = true; if (k == KeyEvent.VK_PAGE_DOWN) pageDownKey = true;
        if (k == KeyEvent.VK_T) tKey = true; if (k == KeyEvent.VK_G) gKey = true;
        if (k == KeyEvent.VK_F) fKey = true; if (k == KeyEvent.VK_H) hKey = true;
        if (k >= KeyEvent.VK_0 && k <= KeyEvent.VK_9) {
            num = k - KeyEvent.VK_0;
        }
    }
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_UP) up = false; if (k == KeyEvent.VK_DOWN) down = false;
        if (k == KeyEvent.VK_LEFT) left = false; if (k == KeyEvent.VK_RIGHT) right = false;
        if (k == KeyEvent.VK_W) wKey = false; if (k == KeyEvent.VK_S) sKey = false;
        if (k == KeyEvent.VK_A) aKey = false; if (k == KeyEvent.VK_D) dKey = false;
        if (k == KeyEvent.VK_PAGE_UP) pageUpKey = false; if (k == KeyEvent.VK_PAGE_DOWN) pageDownKey = false;
        if (k == KeyEvent.VK_T) tKey = false; if (k == KeyEvent.VK_G) gKey = false;
        if (k == KeyEvent.VK_F) fKey = false; if (k == KeyEvent.VK_H) hKey = false;
    }
    public void keyTyped(KeyEvent e) {}
    public static void main(String[] args) {
        JFrame f = new JFrame("3D Unified Space"); Area p = new Area(); f.add(p);
        f.setSize(800, 600); f.setDefaultCloseOperation(3); f.setVisible(true); p.requestFocusInWindow();
    }
}