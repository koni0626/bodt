import json
import os
import cv2
import datetime

for phase in ['train', 'test']:
	#phase = 'train' # train又はtest
	print(phase)
	path = './{}_data/'.format(phase)
	root_path = '.'

	 # dataset is used to save image information and annotation information of all data
	dataset = {'info': [],'licenses': [],'images': [], 'annotations': [], 'categories': []}

	now = str(datetime.datetime.now())
	dataset['info'].append({'year': 2020, 'version': "", 'description': "", 'contributor': "", 'url': "", 'date_created': now})
	dataset['licenses'].append({'id': 1, 'name': None, 'url': None})
	 
	 # Open category label
	with open(os.path.join(root_path, 'classes.txt')) as f:
		classes = f.read().strip().split()
	 
	 # Establish the correspondence between category labels and numeric ids
	for i, cls in enumerate(classes, 1):
		dataset['categories'].append({'id': i, 'name': cls, 'supercategory': 'None'})
	 
	 # Read the image name of the images folder
	indexes = os.listdir(path)
	 
	 # Statistics Processing the number of pictures
	global count
	count = 0
	 
	 # Read Bbox information
	with open(os.path.join(root_path, '{}_annotation.txt'.format(phase))) as tr:
		annos = tr.readlines()
	 
		for k, index in enumerate(indexes):
			count += 1
			# Read images with opencv to get the width and height of the image
			im = cv2.imread(path + index)
			img_height, img_width, _ = im.shape
	 
			# Add image information to the dataset
			dataset['images'].append({'file_name': index,
									  'id': k,
									  'width': img_width,
									  'height': img_height,
									  'license': 1,
									  'flickr_url': "",
									  'coco_url': "",
									  'data_captured':now})
	 
			for ii, anno in enumerate(annos):
				parts = anno.strip().split()
	 
				# Add a tag if the name of the image and the name of the tag are on
				if parts[0] == index:
					cls_id = parts[1]
					# x_min
					x1 = round(float(parts[2]), 3)
					# y_min
					y1 = round(float(parts[3]), 3)
					# x_max
					x2 = x1 + float(parts[4])
					x2 = round(x2, 3)
					# y_max
					y2 = y1 + float(parts[5])
					y2 = round(y2, 3)
					bbox_width = max(0, x2 - x1)
					bbox_width = round(bbox_width, 3)
					bbox_height = max(0, y2 - y1)
					bbox_height = round(bbox_height,3)
					dataset['annotations'].append({
						'area': bbox_width * bbox_height,
						'bbox': [x1, y1, bbox_width, bbox_height],
						'category_id': int(cls_id)+1,
						'id': ii,
						'image_id': k,
						'iscrowd': 0,
						# mask, the rectangle is the four vertices clockwise from the top left corner
						'segmentation': [[x1, y1, x2, y1, x2, y2, x1, y2]]
					})
	 
			print('{} images handled'.format(count))
	 
	 #Save the resulting folder
	folder = os.path.join(root_path, 'annotations')
	if not os.path.exists(folder):
	  os.makedirs(folder)
	json_name = os.path.join(root_path,'annotations/instances_{}_data.json'.format(phase))
	with open(json_name, 'w') as f:
	  json.dump(dataset, f)
