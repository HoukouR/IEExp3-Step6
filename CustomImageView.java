package ieexp3.id190441148.ieexp3_step6;

import androidx.appcompat.widget.AppCompatImageView;
import android.content.Context;
import android.util.AttributeSet;

public class CustomImageView extends AppCompatImageView{

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}