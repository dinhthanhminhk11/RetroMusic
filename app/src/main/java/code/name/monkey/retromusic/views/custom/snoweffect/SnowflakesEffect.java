package code.name.monkey.retromusic.views.custom.snoweffect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class SnowflakesEffect {
    private final Paint particlePaint;
    private final Paint particleThinPaint;
    private final Paint bitmapPaint = new Paint();
    private final Random random = new Random();
    private Bitmap particleBitmap;
    private long lastAnimationTime;
    private final float angleDiff = (float) (Math.PI / 3);
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final ArrayList<Particle> freeParticles = new ArrayList<>();
    private final int viewType;
    private int color;

    private class Particle {
        float x, y, vx, vy, velocity, alpha, lifeTime, currentTime, scale;
        int type;

        void draw(Canvas canvas) {
            if (type == 0) {
                particlePaint.setAlpha((int) (255 * alpha));
                canvas.drawPoint(x, y, particlePaint);
            } else {
                if (particleBitmap == null) {
                    createBitmap();
                }
                bitmapPaint.setAlpha((int) (255 * alpha));
                canvas.save();
                canvas.scale(scale, scale, x, y);
                canvas.drawBitmap(particleBitmap, x, y, bitmapPaint);
                canvas.restore();
            }
        }
    }

    public SnowflakesEffect(int viewType) {
        this.viewType = viewType;
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStrokeWidth(2);
        particlePaint.setStrokeCap(Paint.Cap.ROUND);
        particlePaint.setStyle(Paint.Style.STROKE);

        particleThinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particleThinPaint.setStrokeWidth(1);
        particleThinPaint.setStrokeCap(Paint.Cap.ROUND);
        particleThinPaint.setStyle(Paint.Style.STROKE);

        for (int a = 0; a < 20; a++) {
            freeParticles.add(new Particle());
        }
    }

    private void createBitmap() {
        particleThinPaint.setAlpha(255);
        particleBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(particleBitmap);
        float px = 4;
        float px1 = -1.14f;
        float py1 = 3.1f;
        float angle = -(float) Math.PI / 2;

        for (int a = 0; a < 6; a++) {
            float x = 8;
            float y = 8;
            float x1 = (float) Math.cos(angle) * px;
            float y1 = (float) Math.sin(angle) * px;
            float cx = x1 * 0.66f;
            float cy = y1 * 0.66f;
            bitmapCanvas.drawLine(x, y, x + x1, y + y1, particleThinPaint);

            float angle2 = angle - (float) Math.PI / 2;
            x1 = (float) (Math.cos(angle2) * px1 - Math.sin(angle2) * py1);
            y1 = (float) (Math.sin(angle2) * px1 + Math.cos(angle2) * py1);
            bitmapCanvas.drawLine(x + cx, y + cy, x + x1, y + y1, particleThinPaint);
            x1 = (float) (-Math.cos(angle2) * px1 - Math.sin(angle2) * py1);
            y1 = (float) (-Math.sin(angle2) * px1 + Math.cos(angle2) * py1);
            bitmapCanvas.drawLine(x + cx, y + cy, x + x1, y + y1, particleThinPaint);
            angle += angleDiff;
        }
    }

    private void updateParticles(long dt) {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            if (particle.currentTime >= particle.lifeTime) {
                if (freeParticles.size() < 40) {
                    freeParticles.add(particle);
                }
                particles.remove(i--);
                continue;
            }
            if (viewType == 0) {
                if (particle.currentTime < 200.0f) {
                    particle.alpha = particle.currentTime / 200.0f;
                } else {
                    particle.alpha = 1.0f - (particle.currentTime - 200.0f) / (particle.lifeTime - 200.0f);
                }
            } else {
                if (particle.currentTime < 200.0f) {
                    particle.alpha = particle.currentTime / 200.0f;
                } else if (particle.lifeTime - particle.currentTime < 2000) {
                    particle.alpha = (particle.lifeTime - particle.currentTime) / 2000;
                }
            }
            particle.x += particle.vx * particle.velocity * dt / 500.0f;
            particle.y += particle.vy * particle.velocity * dt / 500.0f;
            particle.currentTime += dt;
        }
    }

    public void onDraw(View parent, Canvas canvas) {
        if (parent == null || canvas == null) {
            return;
        }

        for (Particle particle : particles) {
            particle.draw(canvas);
        }

        if (particles.size() < (viewType == 0 ? 100 : 300)) {
            for (int i = 0; i < (viewType == 0 ? 1 : 10); i++) {
                if (particles.size() < (viewType == 0 ? 100 : 300) && random.nextFloat() > 0.7f) {
                    Particle newParticle = freeParticles.isEmpty() ? new Particle() : freeParticles.remove(0);
                    newParticle.x = random.nextFloat() * parent.getWidth();
                    newParticle.y = random.nextFloat() * (parent.getHeight() - 20);
                    int angle = random.nextInt(40) - 20 + 90;
                    newParticle.vx = (float) Math.cos(Math.toRadians(angle));
                    newParticle.vy = (float) Math.sin(Math.toRadians(angle));
                    newParticle.alpha = 0.0f;
                    newParticle.currentTime = 0;
                    newParticle.scale = random.nextFloat() * 1.5f;
                    newParticle.type = random.nextInt(2);
                    newParticle.lifeTime = viewType == 0 ? 2000 + random.nextInt(100) : 3000 + random.nextInt(2000);
                    newParticle.velocity = 20.0f + random.nextFloat() * 4.0f;
                    particles.add(newParticle);
                }
            }
        }

        long newTime = System.currentTimeMillis();
        long dt = Math.min(17, newTime - lastAnimationTime);
        updateParticles(dt);
        lastAnimationTime = newTime;
        parent.invalidate();
    }

    public void setColorKey(int key) {
        this.color = key;
        updateColors();
    }

    public void updateColors() {
        final int color = this.color & 0xffe6e6e6;
        if (this.color != color) {
            this.color = color;
            particlePaint.setColor(color);
            particleThinPaint.setColor(color);
        }
    }
}
