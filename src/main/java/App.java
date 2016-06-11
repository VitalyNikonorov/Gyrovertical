import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Quaternion;

/**
 * Created by vitaly on 12.06.16.
 */
public class App extends ApplicationAdapter {

    private ModelBatch batch;
    private PerspectiveCamera camera;
    private ModelInstance boxInstance;
    private CameraInputController controller;
    private Environment environment;


    @Override
    public void create () {
        environment = new Environment();
//        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 2f, 2f, 2f, 1f));

        environment.add(new DirectionalLight().set(Color.BLUE, 1f, -1f, 0f));
        environment.add(new DirectionalLight().set(Color.RED, -1f, -1f, 0f));

        environment.add(new DirectionalLight().set(Color.GREEN, 1f, 1f, 0f));
        environment.add(new DirectionalLight().set(Color.CYAN, -1f, 1f, 0f));

        environment.add(new DirectionalLight().set(Color.GOLD, 0f, 0f, 2f));
        environment.add(new DirectionalLight().set(Color.RED, 0f, 0f, -2f));

        batch = new ModelBatch();
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(5, 5, 5);
        camera.lookAt(0, 0, 0);
        camera.far = 100;
        camera.near = 0.1f;

        controller = new CameraInputController(camera);
        controller.autoUpdate = true;

        Gdx.input.setInputProcessor(controller);

        ObjLoader loader = new ObjLoader();
        Model boxModel = loader.loadModel(Gdx.files.internal("box.obj"));
        boxInstance = new ModelInstance(boxModel);

    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Quaternion q = new Quaternion();
        q.setEulerAngles(0.0f, (float) Main.roll,(float) Main.pitch);

        boxInstance.transform.set(q);

        controller.update();
        batch.begin(camera);
        batch.render(boxInstance, environment);
        batch.end();


    }
}
