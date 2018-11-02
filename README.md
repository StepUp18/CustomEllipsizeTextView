# CustomEllipsizeTextView

## Add to project
Root ```build.gradle```
```
      buildscript {
        repositories {
          jcenter()
        }
      }
```

Module ```build.gradle```

```
dependencies {
    implementation 'com.github.stepup18:customellipsizetextview:1.2.0'
}
```

This is TextView which allows you to use any text as ellipsize.

Use it via xml

```
        <com.github.stepup18.CustomEllipsizeTextView
            android:id="@+id/textView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            android:maxLines="3"
            android:textColor="@android:color/black"
            app:ellipsizeColor="@android:color/white"
            app:ellipsizeText="...More"/>
```

Or you can make the same via code

```          
textView.setEllipsizeColor(ContextCompat.getColor(context, android.R.color.white));
textView.setEllipsizeText("...More", Typeface.BOLD); // Yeah, you can change ellipsize textStyle ;) 
```

<img src="https://user-images.githubusercontent.com/44642515/47911836-f96b4500-deaf-11e8-96ad-1b5847094462.gif" width="260" height="460" />

## Known problems

```android:text``` doesn't work, will be fixed later.

## Links
Based on [this](https://github.com/dinuscxj/EllipsizeTextView) abandoned project with improvements and new features.

## Licence

The MIT License

Copyright (c) 2010-2018 Google, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
