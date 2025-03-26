package code.name.monkey.retromusic.views.custom.snoweffect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class SnowflakesView extends View {

    private SnowflakesEffect snowflakesEffect;

    public SnowflakesView(Context context) {
        super(context);
        init();
    }

    public SnowflakesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnowflakesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        snowflakesEffect = new SnowflakesEffect(1);
        snowflakesEffect.setColorKey(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        snowflakesEffect.onDraw(this, canvas);
    }
}