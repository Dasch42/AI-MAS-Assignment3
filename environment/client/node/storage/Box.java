package client.node.storage;
// Goals have a type
// Agents have a color
// Boxes have a color AND a type
import client.node.Color;

public class Box extends Base{
	public char type;
	public Color color;

	public Box(char t, Color color, int row, int col){ 
		super(row, col);
		this.type = t;
		this.color = color;
	}

	@Override
	public boolean equals( Object obj ) {
		if( getClass() != obj.getClass() )
			return false;
		super.equals( obj );

		Box b = (Box)obj;
		return ( this.type == b.type && this.color == b.color );
	}
}