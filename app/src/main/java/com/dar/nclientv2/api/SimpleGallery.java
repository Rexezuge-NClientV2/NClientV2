package com.dar.nclientv2.api;

import static java.util.Objects.isNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dar.nclientv2.api.components.Gallery;
import com.dar.nclientv2.api.components.GalleryData;
import com.dar.nclientv2.api.components.GenericGallery;
import com.dar.nclientv2.api.components.Page;
import com.dar.nclientv2.api.components.Tag;
import com.dar.nclientv2.api.components.TagList;
import com.dar.nclientv2.api.enums.ImageExt;
import com.dar.nclientv2.api.enums.Language;
import com.dar.nclientv2.api.enums.TagStatus;
import com.dar.nclientv2.async.database.Queries;
import com.dar.nclientv2.components.classes.Size;
import com.dar.nclientv2.files.GalleryFolder;
import com.dar.nclientv2.settings.Global;
import com.dar.nclientv2.utility.LogUtility;
import com.dar.nclientv2.utility.Utility;

import org.jsoup.nodes.Element;

import java.util.Collection;
import java.util.Locale;

public class SimpleGallery
    extends GenericGallery {

    private final String title;

    private final ImageExt thumbnail;

    private final int id;

    private final int mediaId;

    private final Language language;

    @Nullable
    private final TagList tags;

    public SimpleGallery(final Parcel in) {
        title = in.readString();
        id = in.readInt();
        mediaId = in.readInt();
        thumbnail = ImageExt.values()[in.readByte()];
        language = Language.values()[in.readByte()];
        tags = null;
    }

    @SuppressLint("Range")
    public SimpleGallery(final Cursor c) {
        title = c.getString(c.getColumnIndex(Queries.HistoryTable.TITLE));
        id = c.getInt(c.getColumnIndex(Queries.HistoryTable.ID));
        mediaId = c.getInt(c.getColumnIndex(Queries.HistoryTable.MEDIAID));
        thumbnail = ImageExt.values()[c.getInt(c.getColumnIndex(Queries.HistoryTable.THUMB))];
        language = Language.UNKNOWN;
        tags = null;
    }

    public SimpleGallery(Context context, Element e) {
        String temp;
        String tags = e.attr("data-tags").replace(' ', ',');
        this.tags = Queries.TagTable.getTagsFromListOfInt(tags);
        language = Gallery.loadLanguage(this.tags);
        Element a = e.getElementsByTag("a").first();
        temp = a.attr("href");
        id = Integer.parseInt(temp.substring(3, temp.length() - 1));
        a = e.getElementsByTag("img").first();
        temp = a.hasAttr("data-src") ? a.attr("data-src") : a.attr("src");
        mediaId = Integer.parseInt(temp.substring(temp.indexOf("galleries") + 10, temp.lastIndexOf('/')));
        final String imgExtension = temp.substring(temp.lastIndexOf('.') + 1);
        thumbnail = Page.charToExt(imgExtension.charAt(0));
        title = e.getElementsByTag("div").first().text();
        if (context != null && id > Global.getMaxId()) Global.updateMaxId(context, id);
    }

    public SimpleGallery(final Gallery gallery) {
        title = gallery.getTitle();
        mediaId = gallery.getMediaId();
        id = gallery.getId();
        thumbnail = gallery.getThumb();
        language = gallery.getLanguage();
        tags = null;
    }

    private static String extToString(final ImageExt ext) {
        return !isNull(ext) ? ext.getName() : null;
    }

    public boolean hasTag(final Tag tag) {
        return this.tags.hasTag(tag);
    }

    public boolean hasTags(final Collection<Tag> tags) {
        return this.tags.hasTags(tags);
    }

    public boolean hasIgnoredTags(final String s) {
        if (!isNull(tags) && !isNull(s) && !s.isEmpty()) {
            for (final Tag t : tags.getAllTagsList()) {
                if (s.contains(t.toQueryTag(TagStatus.AVOIDED))) {
                    LogUtility.d("Found: " + s + ",," + t.toQueryTag());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(title);
        dest.writeInt(id);
        dest.writeInt(mediaId);
        dest.writeByte((byte) thumbnail.ordinal());
        dest.writeByte((byte) language.ordinal());
        //TAGS AREN'T WRITTEN
    }

    public Uri getThumbnail() {
        if (isNull(thumbnail)) {
            return null;
        }

        return ImageExt.GIF.equals(thumbnail)
            ? Uri.parse(String.format(Locale.US, "https://i1." + Utility.getHost() + "/galleries/%d/1.gif", mediaId))
            : Uri.parse(String.format(Locale.US, "https://t1." + Utility.getHost() + "/galleries/%d/thumb.%s", mediaId, extToString(thumbnail)));
    }

    public int getMediaId() {
        return mediaId;
    }

    public ImageExt getThumb() {
        return thumbnail;
    }

    @Override
    public GalleryFolder getGalleryFolder() {
        return null;
    }

    @Override
    public boolean hasGalleryData() {
        return false;
    }

    @Override
    public GalleryData getGalleryData() {
        return null;
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Type getType() {
        return Type.SIMPLE;
    }

    @Override
    public int getPageCount() {
        return 0;
    }

    @Override
    public boolean isValid() {
        return id > 0;
    }

    @NonNull
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Size getMaxSize() {
        return null;
    }

    @Override
    public Size getMinSize() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "SimpleGallery{" +
            "language=" + language +
            ", title='" + title + '\'' +
            ", thumbnail=" + thumbnail +
            ", id=" + id +
            ", mediaId=" + mediaId +
            '}';
    }

    public static final Creator<SimpleGallery> CREATOR = new Creator<SimpleGallery>() {

        @Override
        public SimpleGallery createFromParcel(final Parcel in) {
            return new SimpleGallery(in);
        }

        @Override
        public SimpleGallery[] newArray(final int size) {
            return new SimpleGallery[size];
        }

    };

}
