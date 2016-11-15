import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;

public class Main {

	public static void main(String[] args) throws UsbDisconnectedException, UsbNotActiveException, UsbNotOpenException, IllegalArgumentException, IOException {
		try {
			UsbServices services = UsbHostManager.getUsbServices();
			UsbHub rootHub = services.getRootUsbHub();
			UsbDevice device = findDevice(rootHub, (short)0xFCCF, (short)0xA001);
			List<?> configs = device.getUsbConfigurations();
			System.out.println("Config Count" + configs.size());
			for(Object obj : configs) {
				UsbConfiguration config = (UsbConfiguration)obj;
				if(config.isActive()) {
					System.out.println("is Active:");
					System.out.println(config.getConfigurationString());
				} else {
					System.out.println("is Not Active:");
					System.out.println(config.getConfigurationString());
				}
			}
			UsbConfiguration configuration = device.getActiveUsbConfiguration();
			UsbInterface iface = configuration.getUsbInterface((byte) 0);
			iface.claim();
			UsbPipe pipe = null;
			try {
				UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x01);
				pipe = endpoint.getUsbPipe();
				pipe.open();
				int sent = pipe.syncSubmit(new byte[] {
						(byte)0xC1, 0, 0, 3, 4, 5, 6, 7,
						9, 1, 2, 3, 4, 5, 6, 7,
						9, 1, 2, 3, 4, 5, 6, 7,
						9, 1, 2, 3, 4, 5, 6, 7,
						9, 1, 2, 3, 4, 5, 6, 7,
						9, 1, 2, 3, 4, 5, 6, 7,
						9, 1, 2, 3, 4, 5, 6, 7,
						9, 1, 2, 3, 4, 5, 6, 7,
						});
				sent = pipe.syncSubmit(new byte[] {
						(byte)0xC3, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x20,
						(byte)0x00, (byte)0x0F, (byte)0x0F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						});
				sent = pipe.syncSubmit(new byte[] {
						(byte)0xC2, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x20,
						(byte)0x00, (byte)0x0F, (byte)0x0F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F,
						});
				sent = pipe.syncSubmit(drawBMPImage("logo3.jpg"));
			    System.out.println(sent + " bytes sent");
			} finally {
				pipe.close();
			    iface.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
	    for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
	    {
	        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
	        if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
	        	return device;
	        }
	        if (device.isUsbHub())
	        {
	            device = findDevice((UsbHub) device, vendorId, productId);
	            if (device != null) return device;
	        }
	    }
	    return null;
	}
	
	public static byte[] drawBMPImage(String BMPFileName) throws IOException {
		List<Byte> datas = new ArrayList<Byte>();
		datas.add((byte)0xC2);
		// X Position
		datas.add((byte)0x00);
		datas.add((byte)0x00);
		// Y Position
		datas.add((byte)0x00);
		datas.add((byte)0x00);
		File file = new File(BMPFileName);
		BufferedImage image = ImageIO.read(file);
		
		// Image width and Height
		datas.add((byte)(image.getWidth() & 0xff));
		datas.add((byte)((image.getWidth() >> 8) & 0xff));
		datas.add((byte)(image.getHeight() & 0xff));
		datas.add((byte)((image.getHeight() >> 8) & 0xff));
		// copy command
		datas.add((byte)0x00);
		
		for (int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
		for (int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
//				datas.add((byte)0xFF);
				int color = image.getRGB(xPixel, yPixel);
				color = RGB888ToRGB565(color);
				datas.add((byte)(color & 0xff));
				if(datas.size() % 64 == 0) {
					datas.add((byte)0x02);
				}
				datas.add((byte)((color >> 8) & 0xff));
				if(datas.size() % 64 == 0) {
					datas.add((byte)0x02);
				}
			}
		}
		while(datas.size() % 64 != 0) {
			datas.add((byte)0xFF);
		}
		return toPrimitives(datas.toArray(new Byte[]{}));
	}
	
	static byte[] toPrimitives(Byte[] oBytes)
	{
	    byte[] bytes = new byte[oBytes.length];

	    for(int i = 0; i < oBytes.length; i++) {
	        bytes[i] = oBytes[i];
	    }

	    return bytes;
	}
	
	static int RGB888ToRGB565(int red, int green, int blue) {
	    final int B = (blue >>> 3) & 0x001F;
	    final int G = ((green >>> 2) << 5) & 0x07E0;
	    final int R = ((red >>> 3) << 11) & 0xF800;

	    return (R | G | B);
	}

	static int RGB888ToRGB565(int aPixel) {
	    //aPixel <<= 8;
	    //System.out.println(Integer.toHexString(aPixel));
	    final int red = (aPixel >> 16) & 0xFF;
	    final int green = (aPixel >> 8) & 0xFF;
	    final int blue = (aPixel) & 0xFF;
	    return RGB888ToRGB565(red, green, blue);
	}
}
