package core;

public class Polygon {
    private final Coord[] vertices;
    private final Line[] edges;
    private final BoundingBox boundingBox;

    public Polygon(Coord... vertices) {
        this.vertices = vertices;
        this.edges = computeEdges(vertices);
        this.boundingBox = BoundingBox.fromPoints(vertices);
    }

    public boolean isInside(Coord point) {
        if (vertices.length < 3) {
            return false;
        }

        int intersections = 0;
        // use -100000 instead of Double.MINUS_INFINITY
        // otherwise all calculations would evaluate to minus infinity, resulting in no intersections
        Line intersectingEdge = new Line(new Coord(-100000, 0), point);
        for (Line edge: getEdges()) {
            if (edge.getIntersectionPoint(intersectingEdge).isPresent()) {
                intersections++;
            }
        }
        return intersections % 2 != 0;
    }

    public boolean isOutside(Coord point) {
        return !this.isInside(point);
    }

    public void translate(double dx, double dy) {
        for (Coord point : vertices) {
            point.translate(dx, dy);
        }
    }

    public Coord[] getVertices() {
        return vertices;
    }

    public Line[] getEdges() { return edges; }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    private static Line[] computeEdges(Coord[] vertices) {
        Line[] edges = new Line[vertices.length];
        for (int i = 0; i < vertices.length - 1; i++) {
            edges[i] = new Line(vertices[i], vertices[i + 1]);
        }
        edges[edges.length - 1] = new Line(vertices[0], vertices[vertices.length - 1]);
        return edges;
    }
}
