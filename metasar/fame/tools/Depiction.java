package fame.tools;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.color.IAtomColorer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.*;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple utility class with some depiction methods.
 *
 * Created by sicho on 10/5/16.
 */
public class Depiction {

    /**
     * generate a PNG file depicting a molecule with SOMs highlighted
     *
     * @param molecule the molecule
     * @param path path to the output file
     * @throws Exception thrown if sth breaks
     */
    public static void generateDepiction(IMolecule molecule, String path) throws Exception {
        // parse SOMs information
        List<SoMInfo> infos = SoMInfo.parseInfoAndUpdateMol(molecule);

        List<Integer> soms = new ArrayList<Integer>();
        for (SoMInfo som : infos) {
            soms.add(som.getAtomID());
        }

        // layout the molecule
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(molecule, false);
        sdg.generateCoordinates();

        // make generators
        List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
        generators.add(new BasicSceneGenerator());
        generators.add(new BasicBondGenerator());
        generators.add(new RingGenerator());
        generators.add(new BasicAtomGenerator());
        generators.add(new AtomNumberGenerator());

        // setup the renderer
        AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());
        RendererModel model = renderer.getRenderer2DModel();
//		model.set(BasicAtomGenerator.CompactAtom.class, true);
//		model.set(BasicAtomGenerator.CompactShape.class, BasicAtomGenerator.Shape.OVAL);
        model.set(BasicAtomGenerator.KekuleStructure.class, true);
        model.set(BasicAtomGenerator.ShowExplicitHydrogens.class, true);
        model.set(AtomNumberGenerator.WillDrawAtomNumbers.class, true);
        model.set(AtomNumberGenerator.ColorByType.class, true);
        class MyColorer implements IAtomColorer {

            @Override
            public Color getAtomColor(IAtom iAtom) {
                if (soms.contains(molecule.getAtomNumber(iAtom) + 1)) {
                    return Color.GREEN;
                }

                if (iAtom.getSymbol().equals("H")) {
                    return Color.GRAY;
                } else if (iAtom.getSymbol().equals("N")) {
                    return Color.BLUE;
                } else if (iAtom.getSymbol().equals("O")) {
                    return Color.RED;
                } else {
                    return Color.BLACK;
                }
            }

            @Override
            public Color getAtomColor(IAtom iAtom, Color color) {
                return getAtomColor(iAtom);
            }
        }
        model.set(AtomNumberGenerator.AtomColorer.class, new MyColorer());

        // get the image
        int side = 3 * 400;
        Image image = new BufferedImage(side, side, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(Color.WHITE);
        g.fill(new Rectangle2D.Double(0, 0, side, side));

        // paint
//        renderer.paint(molecule, new AWTDrawVisitor(g));
        renderer.paint(molecule, new AWTDrawVisitor(g),
                new Rectangle2D.Double(0, 0, side, side), true);
        g.dispose();

        // write to file
        File file = new File(path);
        ImageIO.write((RenderedImage)image, "PNG", file);
    }
}
