package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AftershipTracking {
	/*
	 * URL to scrape and parse to Tracking Object
	 * https://track.aftership.com/fedex/786315412629
	 */
	private static final String trackingURL = "https://track.aftership.com/fedex/786315412629";

	public static void main(String[] args) {
		try {
			Dao trackingDao = new Dao();
			Tracking tracking = trackingDao.scrapeAndParseTracking(trackingURL);
			System.out.println(tracking);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Dao {

	public Tracking scrapeAndParseTracking(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		// show tracking title
		System.out.println(doc.title());

		// initial tracking and list checkpoints
		Tracking tracking = new Tracking();
		List<Checkpoint> checkpointsList = new ArrayList<>();

		// find tracking information by ID attribute
		String _number = doc.select(Constants.TRACKING.DETECT_TRACKING_ID).html();
		String _statusT = doc.select(Constants.TRACKING.DETECT_TRACKING_BLOCK_TAG).eq(0).select("p").html();
		String _signedBy = doc.select(Constants.TRACKING.DETECT_TRACKING_BLOCK_TAG).eq(1).select("p").html();
		String _courierT = doc.select(Constants.TRACKING.DETECT_TRACKING_COURIER).eq(0).select("h2").html();
		tracking.setNumber(_number);
		tracking.setStatus(_statusT);
		tracking.setSignedBy(_signedBy);
		tracking.setCourier(_courierT);

		// read line by line to get each checkpoint
		Elements checkpoints = doc.select(Constants.CHECKPOINT.DETECT_CHECKPOINT_CLASSNAME);
		for (Element cp : checkpoints) {

			// initial new checkpoint object
			Checkpoint checkpoint = new Checkpoint();

			// initial new elements to get checkpoint time
			Elements checkpointTime = cp.select(Constants.CHECKPOINT.DETECT_CHECKPOINT_TIME_CLASSNAME);
			String _date = checkpointTime.select("strong").html();
			String _time = checkpointTime.select(".hint").html();
			checkpoint.setTime(_date.concat(" ").concat(_time));

			// initial new elements to get checkpoint status
			Elements checkpointStatus = cp.select(Constants.CHECKPOINT.DETECT_CHECKPOINT_STATUS_CLASSNAME);
			String classAttr[] = checkpointStatus.attr("class").split("\\s+");
			int lengthClassAttr = classAttr.length;
			String _statusC = classAttr[lengthClassAttr - 1];
			checkpoint.setStatus(_statusC);

			// initial new elements to get checkpoint content, courier and location
			Elements checkpointContent = cp.select(Constants.CHECKPOINT.DETECT_CHECKPOINT_CONTENT_CLASSNAME);
			String _content = checkpointContent.select("strong").html().split("<span")[0];
			String _courier = checkpointContent.select("strong span").html();
			String _location = checkpointContent.select(".hint").html();
			checkpoint.setContent(_content);
			checkpoint.setCourier(_courier);
			checkpoint.setLocation(_location);

			// add this checkpoint to stored list
			checkpointsList.add(checkpoint);
			System.out.println(checkpoint.toString());
		}
		tracking.setCheckpoint(checkpointsList);
		// System.out.println(tracking);
		return tracking;
	}
}

class Constants {

	public static class TRACKING {
		/*
		 * <p id="tracking-number"
		 * class="tracking-number--bar text-xs-center m-b-0">786315412629</p>
		 */
		public static final String DETECT_TRACKING_ID = "#tracking-number";
		/*
		 * <div id="tag-container"
		 * class="clearfix text-xs-center tag-delivered additional-info"><div
		 * class="col-sm-6"><p class="tag text-tight">Delivered</p></div><div
		 * class="col-sm-6"><p class="text-tight">Signed by: Signature not
		 * required</p></div></div>
		 */
		public static final String DETECT_TRACKING_BLOCK_TAG = "#tag-container div";
		/*
		 * <div class="media-right"><a href="https://www.aftership.com/courier/fedex"
		 * class="link--black"><h2 class="h4 notranslate">FedEx</h2></a><a
		 * href="tel:18004633339" class="link--phone">18004633339</a></div>
		 */
		public static final String DETECT_TRACKING_COURIER = ".media-right a";
	}

	public static class CHECKPOINT {
		/*
		 * <li class="checkpoint"><div class="checkpoint__time"><strong>Apr 01,
		 * 2019</strong><div class="hint">12:30 pm</div></div><div
		 * class="checkpoint__icon delivered"></div><div
		 * class="checkpoint__content"><strong>Delivered - Left at front door. Package
		 * delivered to recipient address - release authorized<span
		 * class="checkpoint__courier-name">FedEx</span></strong><div
		 * class="hint">MONROE, NY</div></div></li>
		 */
		public static final String DETECT_CHECKPOINT_CLASSNAME = ".checkpoint";
		public static final String DETECT_CHECKPOINT_TIME_CLASSNAME = ".checkpoint__time";
		public static final String DETECT_CHECKPOINT_STATUS_CLASSNAME = ".checkpoint__icon";
		public static final String DETECT_CHECKPOINT_CONTENT_CLASSNAME = ".checkpoint__content";
	}
}

class Tracking {

	private String number;
	private String status;
	private String signedBy;
	private String courier;
	private List<Checkpoint> checkpoint;

	public Tracking() {
		super();
	}

	public Tracking(String number, String status, String signedBy, String courier, List<Checkpoint> checkpoint) {
		super();
		this.number = number;
		this.status = status;
		this.signedBy = signedBy;
		this.courier = courier;
		this.checkpoint = checkpoint;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSignedBy() {
		return signedBy;
	}

	public void setSignedBy(String signedBy) {
		this.signedBy = signedBy;
	}

	public String getCourier() {
		return courier;
	}

	public void setCourier(String courier) {
		this.courier = courier;
	}

	public List<Checkpoint> getCheckpoint() {
		return checkpoint;
	}

	public void setCheckpoint(List<Checkpoint> checkpoint) {
		this.checkpoint = checkpoint;
	}

	@Override
	public String toString() {
		return "Tracking [Number=" + number + "; Status=" + status + "; SignedBy=" + signedBy + "; Courier=" + courier
				+ "; Checkpoint=" + checkpoint + "]";
	}
}

class Checkpoint {
	// append date and time
	private String time;
	private String status;
	private String content;
	private String courier;
	private String location;

	public Checkpoint() {
		super();
	}

	public Checkpoint(String time, String status, String content, String courier, String location) {
		super();
		this.time = time;
		this.status = status;
		this.content = content;
		this.courier = courier;
		this.location = location;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCourier() {
		return courier;
	}

	public void setCourier(String courier) {
		this.courier = courier;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Checkpoint [Time=" + time + "; Status=" + status + "; Content=" + content + "; Courier=" + courier
				+ "; Location=" + location + "]";
	}
}