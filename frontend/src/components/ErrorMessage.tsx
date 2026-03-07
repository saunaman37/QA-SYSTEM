type Props = {
  message: string;
};

export default function ErrorMessage({ message }: Props) {
  return <p style={{ color: 'red', fontSize: '0.875rem', marginTop: '0.25rem' }}>{message}</p>;
}
