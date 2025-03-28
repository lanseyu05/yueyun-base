import React, { useState } from 'react';
import { Upload, message, Button, Modal, Progress } from 'antd';
import { UploadOutlined, PaperClipOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons';
import type { UploadFile, UploadProps } from 'antd/es/upload/interface';
import { request } from 'umi';

interface FileUploadProps {
  /**
   * 上传接口地址
   */
  action?: string;
  /**
   * 存储桶名称
   */
  bucketName?: string;
  /**
   * 接受上传的文件类型
   */
  accept?: string;
  /**
   * 上传文件大小限制（MB）
   */
  maxSize?: number;
  /**
   * 是否支持多文件上传
   */
  multiple?: boolean;
  /**
   * 是否禁用上传
   */
  disabled?: boolean;
  /**
   * 文件列表
   */
  fileList?: UploadFile[];
  /**
   * 文件状态改变回调
   */
  onChange?: (fileList: UploadFile[]) => void;
  /**
   * 上传成功回调
   */
  onSuccess?: (response: any, file: UploadFile) => void;
}

/**
 * 文件上传组件
 * 基于Ant Design Upload，支持MinIO和阿里云OSS
 */
const FileUpload: React.FC<FileUploadProps> = ({
  action = '/storage/upload',
  bucketName,
  accept,
  maxSize = 50,
  multiple = false,
  disabled = false,
  fileList = [],
  onChange,
  onSuccess,
}) => {
  const [previewVisible, setPreviewVisible] = useState<boolean>(false);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [previewTitle, setPreviewTitle] = useState<string>('');

  // 构建上传接口地址
  const uploadUrl = bucketName ? `${action}/${bucketName}` : action;

  // 预览图片
  const handlePreview = async (file: UploadFile) => {
    // 如果文件有预览URL直接使用
    if (file.url) {
      setPreviewUrl(file.url);
      setPreviewVisible(true);
      setPreviewTitle(file.name || '文件预览');
      return;
    }

    // 如果没有URL但有objectName，则获取临时访问URL
    if (file.response?.objectName) {
      try {
        const result = await request(`/storage/url?objectName=${file.response.objectName}`);
        if (result.fileUrl) {
          setPreviewUrl(result.fileUrl);
          setPreviewVisible(true);
          setPreviewTitle(file.name || '文件预览');
        }
      } catch (error) {
        message.error('获取文件预览地址失败');
      }
    }
  };

  // 关闭预览
  const handleCancel = () => setPreviewVisible(false);

  // 上传前检查
  const beforeUpload = (file: File) => {
    // 检查文件大小
    const isLessThanLimit = file.size / 1024 / 1024 < maxSize;
    if (!isLessThanLimit) {
      message.error(`文件大小不能超过 ${maxSize}MB!`);
      return Upload.LIST_IGNORE;
    }
    return true;
  };

  // 自定义请求实现
  const customRequest: UploadProps['customRequest'] = async (options) => {
    const { file, onProgress, onSuccess: onUploadSuccess, onError } = options;
    
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file as Blob);
    
    try {
      // 使用fetch API上传，以便实现进度显示
      const xhr = new XMLHttpRequest();
      xhr.open('POST', uploadUrl, true);
      
      // 监听上传进度
      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          const percent = Math.round((e.loaded / e.total) * 100);
          onProgress?.({ percent });
        }
      };
      
      // 处理上传完成事件
      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          const response = JSON.parse(xhr.responseText);
          onUploadSuccess?.(response, xhr);
          onSuccess?.(response, file as UploadFile);
        } else {
          onError?.(new Error('上传失败'));
        }
      };
      
      // 处理上传错误
      xhr.onerror = () => {
        onError?.(new Error('上传失败'));
      };
      
      // 发送请求
      xhr.send(formData);
    } catch (error) {
      onError?.(error as Error);
    }
  };

  const uploadProps: UploadProps = {
    name: 'file',
    multiple,
    disabled,
    accept,
    fileList,
    beforeUpload,
    customRequest,
    onChange: (info) => {
      const { fileList: newFileList } = info;
      onChange?.(newFileList);
      
      // 处理上传完成逻辑
      const file = info.file;
      if (file.status === 'done') {
        const response = file.response;
        if (response?.fileUrl) {
          message.success(`${file.name} 上传成功`);
          file.url = response.fileUrl;
        } else {
          message.error(`${file.name} 上传失败`);
        }
      } else if (file.status === 'error') {
        message.error(`${file.name} 上传失败`);
      }
    },
    onPreview: handlePreview,
    itemRender: (originNode, file, fileList, { download }) => {
      return (
        <div className="ant-upload-list-item-container">
          {originNode}
          <div className="ant-upload-list-item-actions">
            {file.status === 'done' && file.url && (
              <Button 
                type="link" 
                size="small" 
                icon={<DownloadOutlined />} 
                onClick={() => download?.(file)}
              />
            )}
            {file.status === 'done' && (
              <Button 
                type="link" 
                size="small" 
                icon={<PaperClipOutlined />} 
                onClick={() => handlePreview(file)}
              />
            )}
          </div>
        </div>
      );
    }
  };

  return (
    <>
      <Upload {...uploadProps}>
        <Button icon={<UploadOutlined />} disabled={disabled}>
          上传文件
        </Button>
      </Upload>
      
      <Modal
        open={previewVisible}
        title={previewTitle}
        footer={null}
        onCancel={handleCancel}
      >
        {previewUrl && (
          previewUrl.match(/\.(jpg|jpeg|png|gif)$/i) ? (
            <img alt={previewTitle} style={{ width: '100%' }} src={previewUrl} />
          ) : (
            <div style={{ textAlign: 'center' }}>
              <p>当前文件不支持预览，请下载后查看</p>
              <Button type="primary" href={previewUrl} target="_blank">
                下载文件
              </Button>
            </div>
          )
        )}
      </Modal>
    </>
  );
};

export default FileUpload; 