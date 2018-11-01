package com.github.customellipsizetextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.stepup18.CustomEllipsizeTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.firstTextView)
    CustomEllipsizeTextView firstTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        firstTextView.setText("идентификатор категории (0 – популярное, 1 – шоу, 2 – сериалы, 3 – программы, 4 – фильмы, 5 – мультфильмы). Параметр не является обязательным.\n" +
                "Выдача результатов запроса по параметру category_id настроена в соответствии с тем, как заданы категории для видеороликов конкретного Партнера. Если в запросе отсутствует параметр category_id, ответ будет содержать информацию о контенте всех категорий, кроме популярное. Содержимое категории “Популярное” (“0”) выводится в ответе только, если в запросе присутствует соответствующее значение category_id=“0”. Причем категории контента соответствуют тем, которые заданы в настройках данного Партнера (вкладка Управление проектами на странице редактирования Партнера системы администрирования Videomore)");
    }

    @OnClick(R.id.firstTextView)
    public void onViewClicked() {
        if (firstTextView.getMaxLines() == Integer.MAX_VALUE) {
            firstTextView.setMaxLines(3);
        } else {
            firstTextView.setMaxLines(Integer.MAX_VALUE);
        }
    }
}
