/* Copyright 2020 Redwarp
 * Copyright 2020 GifWallpaper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.redwarp.gifwallpaper

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.redwarp.gifwallpaper.drawable.GifDrawable

class Gif private constructor(gif: Any) {
    private constructor(animatedImageDrawable: AnimatedImageDrawable) : this(animatedImageDrawable as Any)
    private constructor(gifDrawable: GifDrawable) : this(gifDrawable as Any)

    val animatable: Animatable = gif as Animatable
    val drawable: Drawable = gif as Drawable

    init {
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    fun recycle() {
        when (drawable) {
            is GifDrawable -> drawable.recycle()
        }
    }

    companion object {
        suspend fun loadGif(context: Context, uri: Uri): Gif? {
            return withContext(Dispatchers.IO) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Gif(getAnimatedImageDrawable(context, uri) ?: return@withContext null)
                    } else {
                        Gif(getGifDrawable(context, uri) ?: return@withContext null)
                    }
                } catch (exception: java.lang.Exception) {
                    null
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        private fun getAnimatedImageDrawable(
            context: Context,
            uri: Uri
        ): AnimatedImageDrawable? {
            return ImageDecoder.decodeDrawable(
                ImageDecoder.createSource(context.contentResolver, uri)
            ) as? AnimatedImageDrawable
        }

        private fun getGifDrawable(context: Context, uri: Uri): GifDrawable? {
            return context.contentResolver.openInputStream(uri)?.use {
                return GifDrawable.decode(context, it.readBytes())
            }
        }
    }
}
